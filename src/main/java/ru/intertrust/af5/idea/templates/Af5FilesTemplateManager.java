package ru.intertrust.af5.idea.templates;

import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class Af5FilesTemplateManager implements FileTemplateGroupDescriptorFactory {
    public static final String AF5_CONFIG_FILE = "AF5 Config.xml";
    public static final String AF_MODULE_FILE = "AF5 Module.xml";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("AF5", AllIcons.FileTypes.Xml);
        group.addTemplate(new FileTemplateDescriptor(AF5_CONFIG_FILE, AllIcons.FileTypes.Xml));
        group.addTemplate(new FileTemplateDescriptor(AF_MODULE_FILE, AllIcons.FileTypes.Xml));
        return group;
    }
}