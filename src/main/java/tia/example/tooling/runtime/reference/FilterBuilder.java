package tia.example.tooling.runtime.reference;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FilterBuilder {

    private Set<FilterBuilderNamespace> namespaces = new HashSet<>(4);


    public static FilterBuilder create(){
        return new FilterBuilder();
    }


    public FilterBuilderNamespace withNamespace(String namespace) {
        FilterBuilderNamespace currentNS = new FilterBuilderNamespace(namespace);
        namespaces.add(currentNS);
        return currentNS;
    }


    private ElementPattern<? extends PsiElement> build() {
        ElementPattern<XmlAttributeValue> pattern = null;

        for (FilterBuilderNamespace ns : namespaces) {
            Map<String, Set<String>> att2tag = groupByAttribute(ns.pathsToAttributes);

            for (Map.Entry<String, Set<String>> entry : att2tag.entrySet()) {
                Set<String> tags = entry.getValue();
                String[] tagNames = tags.toArray(new String[tags.size()]);
                XmlAttributeValuePattern p = XmlPatterns.xmlAttributeValue().withLocalName(entry.getKey())
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(tagNames).withNamespace(ns.namespace));
                if (pattern != null){
                    pattern = StandardPatterns.or(pattern, p);
                } else {
                    pattern = p;
                }

            }
        }

        return pattern;
    }

    @NotNull
    private Map<String, Set<String>> groupByAttribute(List<XmlIdPath> pathsToAttributes) {
        Map<String, Set<String>> att2tag = new HashMap<>(pathsToAttributes.size());
        for (XmlIdPath attribute : pathsToAttributes) {
            att2tag.compute(attribute.getIdAttribute(), (k,v) -> {
                Set<String> set = v;
                if (set == null) {
                    set = new HashSet<>();
                }
                set.add(attribute.getToTag());
                return set;
            });
        }
        return att2tag;
    }

    public class FilterBuilderNamespace{
        private final String namespace;
        private final List<XmlIdPath> pathsToAttributes = new ArrayList<>();

        FilterBuilderNamespace(String namespace){

            this.namespace = namespace;
        }
        public FilterBuilderNamespace withNamespace(String namespace){
            return FilterBuilder.this.withNamespace(namespace);
        }

        public FilterBuilderNamespace addIdPath(String tagName, String idAttributeName){
            XmlIdPath attribute = new XmlIdPath(namespace, tagName, idAttributeName);
            pathsToAttributes.add(attribute);
            return this;
        }
        public ElementPattern<? extends PsiElement> build() {
            return FilterBuilder.this.build();
        }

    }
}

/*
interface FilterBuilderNamespace {
    FilterBuilder withNamespace(String namespace);
}*/
