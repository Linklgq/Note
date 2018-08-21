package com.example.lenovo.note.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class Folder extends DataSupport{
    private int id;
    private String folderName;

    public int getId() {
        return id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
