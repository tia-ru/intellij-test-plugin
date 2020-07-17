package tia.example.tooling.runtime.reference;

import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
   Search for reference target
 */
class ConfigXmlPsiReference extends XmlAttributeReferenceBase<XmlAttributeValue> {

    public ConfigXmlPsiReference(@NotNull XmlAttributeValue ref, List<XmlIdPath> pathsToAttributes, XmlIdCache cache){
        super(ref, pathsToAttributes, cache);
    }

    @Override
    protected String getRef() {
        return myElement.getValue();
    }

}
