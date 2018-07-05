package org.anhcraft.BitStorage.Utils;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public class UTF8BOMCleaner {
    public static final String UTF8_BOM = "\uFEFF";

    public static String a(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
