package com.example.lenovo.note.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class NoteFolder extends DataSupport {
    private int id;
    private int noteId;
    private int folderId;

    public int getId() {
        return id;
    }

    public int getNoteId() {
        return noteId;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }
}
