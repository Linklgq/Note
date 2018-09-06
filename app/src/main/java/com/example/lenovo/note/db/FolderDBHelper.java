package com.example.lenovo.note.db;

import android.content.ContentValues;
import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderDBHelper {
    public static final int NAME_MAX_LENGTH=32;

    private static final String TAG = "FolderDBHelper";

    private static List<Folder> sFolderList=null;
    private static List<Integer> sCountList=new ArrayList<>();

    private static boolean sFiltering=false;
    private static String sQueryText;

    /** 根据位置获取对应的便签夹*/
    public static Folder get(int position){
        if(sFolderList==null){
            query();
        }
        return sFolderList.get(position);
    }

    /** 获取便签夹中的便签数*/
    public static int getNoteCount(int position){
        // FIXME: 2018/8/20 性能？
        if(sCountList.isEmpty()){
            int size=folderCount();
            for(int i=0;i<size;i++){
//                sCountList.add(DataSupport
//                        .where("folderId = ?",String.valueOf(get(i).getId()))
//                        .count(NoteFolder.class));
                sCountList.add(DataSupport
                        .where("id in (select noteId from NoteFolder " +
                                "where folderId = ?) and isRemoved = 0",String.valueOf(get(i).getId()))
                        .count(Note.class));
            }
        }
        return sCountList.get(position);
    }

    /** 全部便签数*/
    public static int totalNotes(){
        return DataSupport
                .where("isRemoved = 0")
                .count(Note.class);
    }

    /** 新建便签夹*/
    public static boolean add(Folder folder){
//        // 判断是否有重名
//        int count=DataSupport.where("folderName = ?",folder.getFolderName())
//                .count(Folder.class);
//        if(count>0){
//            return false;
//        }
        // 同名检查
        List<Folder> folders=DataSupport.where("id <> ? and folderName = ?",
                String.valueOf(folder.getId()),folder.getFolderName())
                .find(Folder.class);
        if(!folders.isEmpty()){
            Folder f=folders.get(0);
            if(!f.isRemoved()) {    // 已有同名且未删除便签夹
                return false;
            }else{
                // 将标记为删除状态的便签夹下的便签移至新建的便签夹下
                // 删除标记为删除状态的便签夹
                folder.save();
                ContentValues cv=new ContentValues();
                cv.put("folderId",folder.getId());
                DataSupport.updateAll(NoteFolder.class,cv,"folderId = ?",
                        String.valueOf(f.getId()));
                f.delete();
                clearCache();
                return true;
            }
        }
        folder.save();
        // FIXME: 2018/8/20 性能?
        clearCache();
        return true;
    }

    /** 删除便签夹以及便签夹下的所有便签*/
    public static void remove(int position){
        Folder folder=get(position);
        clearNotes(position);
        // 便签夹不删除,标记其为删除状态
        folder.setRemoved(true);
        folder.save();
        // FIXME: 2018/8/20 性能?
        clearCache();
    }

    /** 修改便签夹名字*/
    public static boolean update(int position,String newName){
        Folder folder=get(position);
        // 同名检查
        List<Folder> folders=DataSupport.where("id <> ? and folderName = ?",
                String.valueOf(folder.getId()),newName)
                .find(Folder.class);
        if(!folders.isEmpty()){
            Folder f=folders.get(0);
            if(!f.isRemoved()) {    // 已有同名且未删除便签夹
                return false;
            }else{
                // 将标记为删除状态的便签夹下的便签移至改名的便签夹下
                // 删除标记为删除状态的便签夹
                ContentValues cv=new ContentValues();
                cv.put("folderId",folder.getId());
                DataSupport.updateAll(NoteFolder.class,cv,"folderId = ?",
                        String.valueOf(f.getId()));
                f.delete();
            }
        }
        folder.setFolderName(newName);
        folder.save();
        sFolderList=null;
        return true;
    }

    /** 清空便签夹下的便签*/
    public static void clearNotes(int position){
        int folderId=get(position).getId();
        // 将便签夹下便签全部移至回收站
        List<Note> notes= NoteDBHelper.query(folderId,false,false);
        for(Note note:notes){
            NoteDBHelper.remove(note,false);
        }
        clearCache();
    }

    /** 清空缓存*/
    public static void clearCache(){
        sFolderList=null;
        sCountList.clear();
    }

    /** 便签夹个数*/
    public static int folderCount(){
        if(sFolderList==null){
            query();
        }
//        Log.d(TAG, "folderCount: "+sFolderList.size());
        return sFolderList.size();
    }

    /** 便签夹按名字排序的排名*/
    public static int getRank(int id){
        int rank=0;
        rank=DataSupport
                .where("isRemoved = 0 and folderName < (select folderName from Folder where id = ?)",
                        String.valueOf(id))
                .count(Folder.class);
        return rank;
    }

    /** 通过便签夹id查询便签夹*/
    public static Folder findByFolderId(int id){
        Folder result=DataSupport.find(Folder.class,id);
        if(result!=null&&!result.isRemoved()){
            return result;
        }
        return null;
    }

    /** 设置查询过滤*/
    public static void setFilter(boolean sFiltering,String sQueryText) {
        FolderDBHelper.sFiltering = sFiltering;
        FolderDBHelper.sQueryText = sQueryText;
        if(sFiltering&&TextUtils.isEmpty(sQueryText)){
            FolderDBHelper.sFiltering=false;
        }
        clearCache();
    }

    /** 查询，更新缓存*/
    public static void query(){
        if(sFiltering){
            sFolderList=DataSupport
                    .where("isRemoved = 0 and folderName like ?",
                            "%"+sQueryText+"%")
                    .order("folderName").find(Folder.class);
        }else{
            sFolderList= DataSupport
                    .where("isRemoved = 0")
                    .order("folderName").find(Folder.class);
        }
        sCountList.clear();
    }
}
