package com.example.lenovo.note.db;

import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static org.litepal.crud.DataSupport.order;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderDBUtil {
    public static final int NAME_MAX_LENGTH=32;

    private static final String TAG = "FolderDBUtil";

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
                sCountList.add(DataSupport
                        .where("folderId = ?",String.valueOf(get(i).getId()))
                        .count(NoteFolder.class));
            }
        }
        return sCountList.get(position);
    }

    /** 全部便签数*/
    public static int totalNotes(){
        return DataSupport.count(Note.class);
    }

    /** 新建便签夹*/
    public static boolean add(Folder folder){
        // 判断是否有重名
        int count=DataSupport.where("folderName = ?",folder.getFolderName())
                .count(Folder.class);
        if(count>0){
            return false;
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
        folder.delete();
        // FIXME: 2018/8/20 性能?
        clearCache();
    }

    /** 修改便签夹名字*/
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
        sFolderList=null;
        return true;
    }

    /** 清空便签夹下的便签*/
    public static void clearNotes(int position){
        int folderId=get(position).getId();
        // 不仅要删除便签以及便签的目录项，还要删除便签附带的图片
        // 所以先查询，再逐个删除
        List<Note> notes= NoteDBUtil.query(folderId);
        for(Note note:notes){
            NoteDBUtil.remove(note);
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
        return sFolderList.size();
    }

    /** 便签夹按名字排序的排名*/
    public static int getRank(int id){
        int rank=0;
        rank=DataSupport
                .where("folderName < (select folderName from Folder where id = ?)",
                        String.valueOf(id))
                .count(Folder.class);
        return rank;
    }

    /** 通过便签夹id查询便签夹*/
    public static Folder findByFolderId(int id){
        Folder result=DataSupport.find(Folder.class,id);
        return result;
    }

    /** 设置查询过滤*/
    public static void setFilter(boolean sFiltering,String sQueryText) {
        FolderDBUtil.sFiltering = sFiltering;
        FolderDBUtil.sQueryText = sQueryText;
        if(sFiltering&&TextUtils.isEmpty(sQueryText)){
            FolderDBUtil.sFiltering=false;
        }
        clearCache();
    }

    /** 查询，更新缓存*/
    public static void query(){
        if(sFiltering){
            sFolderList=DataSupport.where("folderName like ?","%"+sQueryText+"%")
                    .order("folderName").find(Folder.class);
        }else{
            sFolderList= order("folderName").find(Folder.class);
        }
        sCountList.clear();
    }
}
