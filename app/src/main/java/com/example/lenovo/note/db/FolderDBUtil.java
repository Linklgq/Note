package com.example.lenovo.note.db;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderDBUtil {
    public static final int NAME_MAX_LENGTH=32;

    private static final List<Folder> folderList=new ArrayList<>();
    private static final List<Integer> countList=new ArrayList<>();

    public static Folder get(int position){
        if(folderList.isEmpty()){
            List<Folder> result= DataSupport.order("folderName").find(Folder.class);
            for(Folder folder:result){
                folderList.add(folder);
            }
        }
        return folderList.get(position);
    }

//    /** 获取便签夹中的便签数*/
//    public static int getCount(int folderId){
//        return DataSupport.where("folderId = ?",String.valueOf(folderId)).count(NoteFolder.class);
//    }

    /** 获取便签夹中的便签数*/
    public static int getNoteCount(int position){
        // FIXME: 2018/8/20 性能？
        if(countList.isEmpty()){
            int size=folderCount();
            for(int i=0;i<size;i++){
                countList.add(DataSupport
                        .where("folderId = ?",String.valueOf(get(i).getId()))
                        .count(NoteFolder.class));
            }
        }
        return countList.get(position);
    }

    public static int totalNotes(){
        return DataSupport.count(Note.class);
    }

    public static void add(Folder folder){
        folder.save();
        // FIXME: 2018/8/20 性能?
        clearCache();
    }

    public static void remove(int position){
        Folder folder=get(position);
        clearNotes(position);
        folder.delete();
        // FIXME: 2018/8/20 性能?
        clearCache();
    }

    public static boolean update(int position,String newName){
        Folder folder=get(position);
        // 同名检查
        int count=DataSupport.where("id <> ? and folderName = ?",
                String.valueOf(folder.getId()),newName)
                .count(Folder.class);
        if(count>0){
            return false;
        }
        folder.setFolderName(newName);
        folder.save();
        folderList.clear();
        return true;
    }

    public static void clearNotes(int position){
        int folderId=get(position).getId();
//        DataSupport.deleteAll(Note.class,
//                "id in (select noteId from NoteFolder where folderId = ?)",
//                String.valueOf(folderId));
//        DataSupport.deleteAll(NoteFolder.class,
//                "folderId = ?",String.valueOf(folderId));
        // 不仅要删除便签以及便签的目录项，还要删除便签附带的图片
        // 所以先查询，再逐个删除
        List<Note> notes=NoteDBUtil.query(folderId);
        for(Note note:notes){
            NoteDBUtil.remove(note);
        }
        clearCache();
    }

    public static void clearCache(){
        folderList.clear();
        countList.clear();
    }

    public static int folderCount(){
        if(folderList.isEmpty()){
            List<Folder> result= DataSupport.order("folderName").find(Folder.class);
            for(Folder folder:result){
                folderList.add(folder);
            }
        }
        return folderList.size();
    }

    public static int getRank(int id){
        int rank=0;
        rank=DataSupport
                .where("folderName < (select folderName from Folder where id = ?)",
                        String.valueOf(id))
                .count(Folder.class);
        return rank;
    }

    public static Folder findByFolderId(int id){
        Folder result=DataSupport.find(Folder.class,id);
        return result;
    }
}
