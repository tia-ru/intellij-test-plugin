package tia.example.tooling.runtime.reference;

import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
   Search for reference target
 */
class ConfigXmlPsiReference extends XmlAttributeReferenceBase<XmlAttributeValue> {

    public ConfigXmlPsiReference(@NotNull XmlAttributeValue ref, List<ConfigXmlAttribute> pathsToAttributes){
        super(ref, pathsToAttributes);
    }

    @Override
    protected String getRef() {
        return myElement.getValue();
    }

}
