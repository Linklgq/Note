package com.example.lenovo.note.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Lenovo on 2018/7/26.
 */

public class Note extends DataSupport {
    private int id;
    private long createdTime;
    private long modifiedTime;
    private String content;

    public Note(){
        long currentTime=System.currentTimeMillis();
        createdTime=currentTime;
        modifiedTime=currentTime;
        content="";
    }

    public int getId() {
        return id;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
