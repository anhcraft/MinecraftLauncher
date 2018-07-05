package org.anhcraft.BitStorage.Machine;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public class DataViewer {
    private List<String> file;
    private LinkedHashMap<String, Object> data;
    private LinkedHashMap<Integer, CommentData> oomments;
    private LinkedHashMap<String, Integer> sections;

    public DataViewer(LinkedHashMap<String, Object> data, List<String> file, LinkedHashMap<Integer, CommentData> oomments, LinkedHashMap<String, Integer> sections){
        this.data = data;
        this.file = file;
        this.oomments = oomments;
        this.sections = sections;
    }

    public List<String> getFile(){
        return file;
    }
    public void setFile(List<String> a){
        file = a;
    }
    public LinkedHashMap<String, Object> getData(){
        return data;
    }
    public void setData(LinkedHashMap<String, Object> a){
        data = a;
    }
    public LinkedHashMap<Integer, CommentData> getComments(){
        return oomments;
    }
    public void setComments(LinkedHashMap<Integer, CommentData> a){
        oomments = a;
    }
    public LinkedHashMap<String, Integer> getSections(){
        return sections;
    }
    public void setSections(LinkedHashMap<String, Integer> a){
        sections = a;
    }
}
