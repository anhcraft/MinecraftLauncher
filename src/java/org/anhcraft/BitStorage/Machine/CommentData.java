package org.anhcraft.BitStorage.Machine;

/**
 * Copyright (c) by Anh Craft. All rights reserved.
 * Licensed under the apache license v2.0.
 */
public class CommentData {
    private String ct;
    private Boolean inline;
    private int contentLine;

    public CommentData(String ct, Boolean inline, int contentLine){
        this.ct = ct;
        this.inline = inline;
        this.contentLine = contentLine;
    }

    public String getContent(){
        return ct;
    }
    public void setContent(String a){
        ct = a;
    }
    public Boolean getInline(){
        return inline;
    }
    public void setInline(Boolean a){
        inline = a;
    }
    public int getContentLine(){
        return contentLine;
    }
    public void setContentLine(int a){
        contentLine = a;
    }
}
