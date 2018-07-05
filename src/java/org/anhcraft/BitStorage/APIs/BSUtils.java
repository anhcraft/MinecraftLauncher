package org.anhcraft.BitStorage.APIs;

import java.util.List;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public interface BSUtils {
    void set(String path, Object value);
    String getString(String path);
    int getInteger(String path);
    float getFloat(String path);
    Boolean getBoolean(String path);
    List<String> getStringList(String path);
    void scan();
    void printContent();
}