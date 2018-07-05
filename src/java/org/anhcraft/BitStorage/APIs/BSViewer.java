package org.anhcraft.BitStorage.APIs;

import org.anhcraft.BitStorage.Machine.CommentData;
import org.anhcraft.BitStorage.Machine.DataViewer;
import org.anhcraft.BitStorage.Machine.Handle;
import org.anhcraft.BitStorage.Reader.UTF8FromFile;
import org.anhcraft.BitStorage.Reader.UTF8FromInput;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public class BSViewer implements BSUtils {
    private DataViewer viewer;
    private List<String> cont;
    private LinkedHashMap<String, Object> result;
    private LinkedHashMap<Integer, CommentData> comments;
    private LinkedHashMap<String, Integer> sections;

    public BSViewer(File f){
        cont = new UTF8FromFile(f).c();
        viewer = Handle.a(cont);
        result = viewer.getData();
        comments = viewer.getComments();
        sections = viewer.getSections();
    }

    public BSViewer(Class f, String path){
        cont = new UTF8FromInput(f, path).c();
        viewer = Handle.a(cont);
        result = viewer.getData();
        comments = viewer.getComments();
        sections = viewer.getSections();
    }

    @Override
    public void set(String path, Object value) {
        if(0 < path.replaceAll(" ", "").length()){
            if(sections.containsKey(path)) {
                result = Handle.j(result, path);
                sections = Handle.k(sections, path);
            }
            if(result.containsKey(path)) {
                result = Handle.j(result, path);
            }
            result.put(path, value);
            viewer.setData(result);
            viewer.setSections(sections);
            cont = Handle.m(viewer);
        }
    }

    @Override
    public String getString(String path){
        Object a = result.get(path);
        if(a instanceof String){
            return (String) a;
        } else {
            return a.toString();
        }
    }

    @Override
    public int getInteger(String path){
        Object a = result.get(path);
        return Integer.parseInt(a.toString());
    }

    @Override
    public float getFloat(String path) {
        Object a = result.get(path);
        return Float.parseFloat(a.toString());
    }

    @Override
    public Boolean getBoolean(String path) {
        Object a = result.get(path);
        return Boolean.parseBoolean(a.toString());
    }

    @Override
    public List<String> getStringList(String path) {
        Object a = result.get(path);
        if(a instanceof List){
            return (List<String>) a;
        } else {
            return null;
        }
    }

    @Override
    public void scan() {
        for(String a : result.keySet()) {
            System.out.println(a + ": " + result.get(a) + " thuộc " + result.get(a).getClass().getName());
        }
        for(String a : sections.keySet()) {
            System.out.println("Phần: " + a);
        }
        for(int a : comments.keySet()) {
            CommentData b = comments.get(a);
            if(b.getInline()) {
                System.out.println(b.getContentLine() + " Ghi chú cùng dòng: " + b.getContent());
            } else {
                System.out.println(b.getContentLine() + " Ghi chú một dòng: " + b.getContent());
            }
        }
    }

    @Override
    public void printContent() {
        for(String a : cont){
            System.out.println(a);
        }
    }
}
