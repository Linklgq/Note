package com.example.lenovo.note.db;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderDBUtil {
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
                        .where("folderId = ?",String.valueOf(folderList.get(i).getId()))
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

    public static void remove(Folder folder){
        folder.save();
        // FIXME: 2018/8/20 性能?
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
}
