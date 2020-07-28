package tia.example.tooling.runtime.util;

import com.intellij.patterns.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class PatternBuilder {

    private Set<Namespace> namespaces = new HashSet<>(4);


    public static PatternBuilder create(){
        return new PatternBuilder();
    }


    public Namespace withNamespace(String namespace, Function<Element, Element> fun) {
        Namespace currentNS = new Namespace(namespace, fun);
        namespaces.add(currentNS);
        return currentNS;
    }


    private XmlAttributeValuePattern build() {
        XmlAttributeValuePattern pattern = null;

        for (Namespace ns : namespaces) {
            XmlAttributeValuePattern p = ns.qualifyAttributesPattern(null);
            if (pattern == null) {
                pattern = p;
            } else if (p != null) {
                pattern = or(pattern, p);
            }
        }

        return pattern;
    }


    public class Element {
        protected final String name;
        protected final String namespace;
        private final Element parent;;
        List<Element> childs = new ArrayList<>();
        protected final List<XmlIdPath> pathsToAttributes = new ArrayList<>();
        private final Function<Element, Element> fun;

        private Element(@NotNull String name, @NotNull String namespace, Element parent, Function<Element, Element> fun){
            this.name = name;
            this.namespace = namespace;
            this.parent = parent;
            this.fun = fun;
        }
        public Element insideTag(String name, Function<Element, Element> fun){
            Element child = new Element(name, namespace, this, fun);
            childs.add(child);
            return this;
        }

        public Element addIdPath(String tagName, String idAttributeName){
            XmlIdPath attribute = new XmlIdPath(namespace, tagName, idAttributeName);
            pathsToAttributes.add(attribute);
            return this;
        }

        protected XmlAttributeValuePattern qualifyTag(int level, XmlAttributeValuePattern pattern){
            pattern = pattern.withSuperParent(level, XmlPatterns.xmlTag().withLocalName(name));
            if (parent != null){
                pattern = parent.qualifyTag(level + 1, pattern);
            }
            return pattern;
        }

        protected XmlAttributeValuePattern qualifyAttributesPattern(XmlAttributeValuePattern attPattern){

            fun.apply(this); // Build child subtree

            Map<String, Set<String>> att2tag = groupByAttribute(pathsToAttributes);

            for (Map.Entry<String, Set<String>> entry : att2tag.entrySet()) {
                Set<String> tags = entry.getValue();
                String[] tagNames = tags.toArray(new String[tags.size()]);
                XmlTagPattern.Capture tagPattern = XmlPatterns.xmlTag().withLocalName(tagNames).withNamespace(namespace);
                XmlAttributeValuePattern p = XmlPatterns.xmlAttributeValue().withLocalName(entry.getKey())
                        .withSuperParent(2, tagPattern);
                p = qualifyTag(3, p);

                if (attPattern == null) {
                    attPattern = p;
                } else {
                    attPattern = or(attPattern, p);
                }
            }

            for (Element child : childs) {
                XmlAttributeValuePattern p = child.qualifyAttributesPattern(attPattern);
                if (attPattern == null) {
                    attPattern = p;
                } else {
                    attPattern = or(attPattern, p);
                }
            }

            return attPattern;
        }


        @Override
        public String toString() {
            return this.getClass().getSimpleName() + '{' +
                    "name='" + name + '\'' +
                    ", parent=" + parent +
                    '}';
        }
    }

    public class Namespace extends Element {

        private Namespace(String namespace, Function<Element, Element> fun){
            super(namespace , namespace, null, fun);
        }

        public Namespace withNamespace(String namespace, Function<Element, Element> fun){
            return PatternBuilder.this.withNamespace(namespace, fun);
        }

        public XmlAttributeValuePattern build() {
            return PatternBuilder.this.build();
        }

        @Override
        protected XmlAttributeValuePattern qualifyTag(int level, XmlAttributeValuePattern pattern) {
            return pattern;
        }

    }

    @NotNull
    private static Map<String, Set<String>> groupByAttribute(List<XmlIdPath> pathsToAttributes) {
        Map<String, Set<String>> att2tag = new HashMap<>(pathsToAttributes.size());
        for (XmlIdPath attribute : pathsToAttributes) {
            att2tag.compute(attribute.getIdAttribute(), (k,set) -> {
                if (set == null) {
                    set = new HashSet<>();
                }
                set.add(attribute.getToTag());
                return set;
            });
        }
        return att2tag;
    }

    @NotNull
    @SafeVarargs
    private static XmlAttributeValuePattern or(@NotNull final XmlAttributeValuePattern... patterns) {
        return new XmlAttributeValuePattern(new InitialPatternConditionPlus(XmlAttributeValue.class) {
            @Override
            public boolean accepts(@Nullable final Object o, final ProcessingContext context) {
                for (final XmlAttributeValuePattern pattern : patterns) {
                    if (pattern.accepts(o, context)) return true;
                }
                return false;
            }

            @Override
            public void append(@NotNull @NonNls final StringBuilder builder, final String indent) {
                boolean first = true;
                for (final ElementPattern pattern : patterns) {
                    if (!first) {
                        builder.append("\n").append(indent);
                    }
                    first = false;
                    pattern.getCondition().append(builder, indent + "  ");
                }
            }

            @Override
            public List<XmlAttributeValuePattern> getPatterns() {
                return Arrays.asList(patterns);
            }
        });
    }
}

/*
interface Namespace {
    PatternBuilder withNamespace(String namespace);
}*/
