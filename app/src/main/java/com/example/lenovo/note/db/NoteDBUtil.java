package com.example.lenovo.note.db;

import android.text.TextUtils;

import com.example.lenovo.note.util.NoteAnalUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Lenovo on 2018/8/21.
 */

public class NoteDBUtil {
    private static final String TAG = "NoteDBUtil";

    private static boolean sFiltering=false;
    private static String sQueryText;

    private static List<Note> sNoteList;
    private static Order sOrder=Order.BY_MODIFIED_TIME;
    private static int sFolderId=-1;

    /** 根据位置获取相应的便签*/
    public static Note get(int position){
        if(sNoteList==null){
            sNoteList=query(sFolderId);
        }
        return sNoteList.get(position);
    }

    /** 修改便签*/
    public static boolean update(Note note){
        // 若便签内容为空白，拒绝修改
        if(NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()){
            return false;
        }else{
            note.save();
            sNoteList=null;
            return true;
        }
    }

    /** 添加便签*/
    public static boolean add(Note note){
        // 若便签内容为空白，拒绝添加
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
            return true;
        }
    }

    /** 删除便签*/
    public static void remove(Note note){
        // 删除便签的图片文件
        NoteAnalUtil.rmText(note.getContent());
        // 删除便签在便签夹下的目录项
        DataSupport.deleteAll(NoteFolder.class,"noteId = ?",String.valueOf(note.getId()));
        note.delete();
        sNoteList=null;
    }

    /** 当前便签夹的便签总数*/
    public static int count(){
        if(sNoteList==null){
            sNoteList=query(sFolderId);
        }
        return sNoteList.size();
    }

    /** 便签在当前便签夹下的排名*/
    public static int getRank(int id){
        String sqlWhere="";
        if(sOrder==Order.BY_CREATED_TIME||sOrder==Order.BY_MODIFIED_TIME){
            sqlWhere=sOrder.toString()+" > (select "+sOrder.toString()
                    +" from Note where id = ?)";
        }else if(sOrder==Order.BY_CONTENT){
            sqlWhere=sOrder.toString()+" < (select "+sOrder.toString()
                    +" from Note where id = ?)";
        }
        int rank;
        if(sFolderId<0){
            rank = DataSupport
                    .where(sqlWhere,String.valueOf(id))
                    .count(Note.class);
        }else {
            sqlWhere="id in (select noteId from NoteFolder where folderId = ?) and "
                    +sqlWhere;
            rank = DataSupport
                    .where(sqlWhere, String.valueOf(sFolderId), String.valueOf(id))
                    .count(Note.class);
        }
        return rank;
    }

    /** 设置排名方式*/
    public static void setsOrder(Order sOrder) {
        NoteDBUtil.sOrder = sOrder;
        sNoteList=null;
    }

    /** 设置当前便签夹*/
    public static void setsFolderId(int sFolderId) {
        NoteDBUtil.sFolderId = sFolderId;
        sNoteList=null;
    }

    /** 设置查询过滤*/
    public static void setFilter(boolean sFiltering,String sQueryText) {
        NoteDBUtil.sFiltering = sFiltering;
        NoteDBUtil.sQueryText = sQueryText;
        if(sFiltering&& TextUtils.isEmpty(sQueryText)){
            NoteDBUtil.sFiltering=false;
        }
        sNoteList=null;
    }


    /** 根据便签夹id查询该便签夹下的所有便签*/
    public static List<Note> query(int folderId){
        if(folderId<0) {
            if(sFiltering){
                return DataSupport.where("content like ?","%"+sQueryText+"%")
                        .order(sOrder.by()).find(Note.class);
            }else {
                return DataSupport.order(sOrder.by()).find(Note.class);
            }
        }else{
            if(sFiltering) {
                return DataSupport
                        .where("id in (select noteId from NoteFolder where folderId = ?)" +
                                        " and content like ?",
                                String.valueOf(folderId), "%" + sQueryText + "%")
                        .order(sOrder.by())
                        .find(Note.class);
            }else{
                return DataSupport
                        .where("id in (select noteId from NoteFolder where folderId = ?)",
                                String.valueOf(folderId))
                        .order(sOrder.by())
                        .find(Note.class);
            }

        }
    }

    /** 更新缓存*/
    public static void query(){
        sNoteList=query(sFolderId);
    }
}
