package tia.example.tooling.runtime.util;

public enum GlobalXmlElements {
    DOMAIN_OBJECT_TYPE,
    FIELD_GROUP;

    private final String xmlName = name().toLowerCase();

    public String getXmlName() {
        return xmlName;
    }
}
