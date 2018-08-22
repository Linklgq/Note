package com.example.lenovo.note.db;

import android.util.Log;

import com.example.lenovo.note.util.NoteAnalUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

import static org.litepal.crud.DataSupport.where;

/**
 * Created by Lenovo on 2018/8/21.
 */

public class NoteDBUtil {
    public enum Order{
        BY_CREATED_TIME("createdTime"){
            @Override
            public String by() {
                return order+" desc";
            }
        },
        BY_MODIFIED_TIME("modifiedTime"){
            @Override
            public String by() {
                return order+" desc";
            }
        },
        BY_CONTENT("content"){
            @Override
            public String by() {
                return order;
            }
        };

        String order;

        Order(String order){
            this.order=order;
        }

        public abstract String by();

        @Override
        public String toString() {
            return order;
        }
    }

    private static final String TAG = "NoteDBUtil";

    private static List<Note> sNoteList;
    private static Order sOrder=Order.BY_MODIFIED_TIME;
    private static int sFolderId=-1;

    public static Note get(int position){
        if(sNoteList==null){
            sNoteList=query(sFolderId);
        }
        return sNoteList.get(position);
    }

    public static boolean update(Note note){
        if(NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()){
            return false;
        }else{
            note.save();
            sNoteList=null;
            return true;
        }
    }

    public static boolean add(Note note){
        if(NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()){
            return false;
        }else{
            note.save();
            sNoteList=null;
            if(sFolderId>=0){
                NoteFolder noteFolder=new NoteFolder();
                noteFolder.setFolderId(sFolderId);
                noteFolder.setNoteId(note.getId());
                noteFolder.save();
            }
            Log.d(TAG, "add: "+sFolderId+" "+note.getId());
            return true;
        }
    }

    public static void remove(Note note){
        NoteAnalUtil.rmText(note.getContent());
//        List<NoteFolder> result=DataSupport.where("noteId = ?",String.valueOf(note.getId()))
//                .find(NoteFolder.class);
//        for(NoteFolder noteFolder:result){
//            noteFolder.delete();
//        }
        DataSupport.deleteAll(NoteFolder.class,"noteId = ?",String.valueOf(note.getId()));
        note.delete();
        sNoteList=null;
    }

    public static int count(){
        if(sNoteList==null){
            sNoteList=query(sFolderId);
        }
        return sNoteList.size();
    }

    public static int getRank(int id){
        int rank=0;
        if(sOrder==Order.BY_CREATED_TIME||sOrder==Order.BY_MODIFIED_TIME){
            rank= where("? > (select ? from Note where id = ?)",
                    sOrder.toString(),sOrder.toString(),String.valueOf(id))
                    .count(Note.class);
        }
        Log.d(TAG, "getRank: "+rank);
        return rank;
    }

    public static void setsOrder(Order sOrder) {
        NoteDBUtil.sOrder = sOrder;
        sNoteList=null;
    }

    public static void setsFolderId(int sFolderId) {
        NoteDBUtil.sFolderId = sFolderId;
        sNoteList=null;
    }

    public static List<Note> query(int folderId){
        if(folderId<0) {
            return DataSupport.order(sOrder.by()).find(Note.class);
        }else{
            return DataSupport
                    .where("id in (select noteId from NoteFolder where folderId = ?)",
                            String.valueOf(sFolderId))
                    .order(sOrder.by())
                    .find(Note.class);

        }
    }
}
