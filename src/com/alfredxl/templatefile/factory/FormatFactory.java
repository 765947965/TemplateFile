package com.alfredxl.templatefile.factory;

import com.alfredxl.templatefile.bean.Template;

import java.util.List;

public class FormatFactory {
    private String baseDir;
    private String packageName;
    private String currentPath;

    public FormatFactory(String baseDir, String sourceRootFilePath, String currentPath) {
        this.baseDir = baseDir;
        if (sourceRootFilePath.equals(currentPath)) {
            this.packageName = "";
        } else {
            this.packageName = currentPath.substring(sourceRootFilePath.length() + 1).replace("/", ".");
        }
        this.currentPath = currentPath;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public String formatData(List<Template> dynamicList, List<Template> defaultDynamicList, String data) {
        for (Template template : defaultDynamicList) {
            if (template.isEnabled()) {
                data = conversion(template.getKey(), template.getValue(), data);
            }
        }
        for (Template template : dynamicList) {
            if (template.isEnabled()) {
                data = conversion(template.getKey(), template.getValue(), data);
            }
        }
        return data;
    }

    private String conversion(String key, String value, String data) {
        String tempKey = "^" + key + "^";
        data = data.replace(tempKey, value.toLowerCase());
        data = data.replace(key, value.toLowerCase());
        return data;
    }
}
