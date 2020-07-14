package tia.example.tooling.runtime.reference;

public class ConfigXmlAttribute {
    private final String toTag;
    private final String idAttribute;

    public ConfigXmlAttribute(String toTag, String idAttribute){
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }

    public String getToTag() {
        return toTag;
    }

    public String getIdAttribute() {
        return idAttribute;
    }
}
