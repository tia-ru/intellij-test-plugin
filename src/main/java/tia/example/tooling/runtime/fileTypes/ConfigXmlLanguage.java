package tia.example.tooling.runtime.fileTypes;

import com.intellij.lang.xml.XMLLanguage;

public class ConfigXmlLanguage extends XMLLanguage {

    public static final ConfigXmlLanguage INSTANCE = new ConfigXmlLanguage();

    public ConfigXmlLanguage(){
       super(XMLLanguage.INSTANCE, "af5-config", "application/xml", "text/xml");
    }
}
