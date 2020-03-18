package tia.example.tooling.runtime.reference;

import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
   Search for reference target
 */
class ConfigXmlPsiReference extends XmlAttributeReferenceBase<XmlAttributeValue> {

    ConfigXmlPsiReference(@NotNull XmlAttributeValue ref, String toTag, String idAttribute) {
        super(ref, toTag, idAttribute);
    }

    @Override
    protected String getRef() {
        return myElement.getValue();
    }

}
