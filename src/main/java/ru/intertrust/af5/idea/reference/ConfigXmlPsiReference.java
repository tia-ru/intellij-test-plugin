package ru.intertrust.af5.idea.reference;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
   Search for reference target
 */
class ConfigXmlPsiReference extends XmlReferenceBase<XmlAttributeValue> {

    public ConfigXmlPsiReference(@NotNull XmlAttributeValue ref,
                                 boolean isInContainingFile, Function<XmlAttributeValue, XmlAttributeValuePattern> patternGenerator,
                                 XmlIdCache cache){

        super(ref, isInContainingFile, patternGenerator, cache);
    }

    @Override
    protected String getRef() {
        return myElement.getValue();
    }

}
