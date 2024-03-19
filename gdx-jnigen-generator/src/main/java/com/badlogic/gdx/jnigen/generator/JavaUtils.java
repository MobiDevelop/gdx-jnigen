package com.badlogic.gdx.jnigen.generator;

public class JavaUtils {

    public static String functionSignatureToName(String func) {
        return func
                .replace("(", "").replace(")", "").replace(", ", "-")
                .replace(" *", "Pointer").replace(" ", "_");
    }

    public static String capitalize(String toCapitalize) {
        return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1);
    }

    public static String javarizeName(String cName) {
        cName = capitalize(cName);
        int index;
        while ((index = cName.indexOf('_')) != -1) {
            cName = cName.substring(0, index) + capitalize(cName.substring(index + 1));
        }
        return cName;
    }
}
