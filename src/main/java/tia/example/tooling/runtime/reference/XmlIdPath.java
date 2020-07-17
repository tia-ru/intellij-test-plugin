package tia.example.tooling.runtime.reference;

public class XmlIdPath {
    private final String namespace;
    private final String toTag;
    private final String idAttribute;

    public XmlIdPath(String namespace, String toTag, String idAttribute){
        this.namespace = namespace;
        this.toTag = toTag;
        this.idAttribute = idAttribute;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getToTag() {
        return toTag;
    }

    public String getIdAttribute() {
        return idAttribute;
    }
}
