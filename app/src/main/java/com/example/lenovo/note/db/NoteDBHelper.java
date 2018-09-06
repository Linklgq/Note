package com.example.lenovo.note.db;

import android.text.TextUtils;

import com.example.lenovo.note.util.NoteAnalUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Lenovo on 2018/8/21.
 */

public class NoteDBHelper {
    public static final int GENERAL = 0;
    public static final int REMOVED = 1;

    private static final String TAG = "NoteDBHelper";

    private static boolean sFiltering = false;
    private static String sQueryText;

    private static int sType = GENERAL;
    private static boolean isRemoved = false;

    private static List<Note> sNoteList;
    private static Order sOrder = Order.BY_MODIFIED_TIME;
    private static int sFolderId = -1;

    /**
     * 根据位置获取相应的便签
     */
    public static Note get(int position) {
        if (sNoteList == null) {
            sNoteList = query(sFolderId);
        }
        return sNoteList.get(position);
    }

    /**
     * 修改便签
     */
    public static boolean update(Note note) {
        // 若便签内容为空白，拒绝修改
        if (NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()) {
            return false;
        } else {
            note.save();
            sNoteList = null;
            return true;
        }
    }

    /**
     * 添加便签
     */
    public static boolean add(Note note) {
        // 若便签内容为空白，拒绝添加
        if (NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()) {
            return false;
        } else {
            note.save();
            sNoteList = null;
            if (sFolderId >= 0) {
                NoteFolder noteFolder = new NoteFolder();
                noteFolder.setFolderId(sFolderId);
                noteFolder.setNoteId(note.getId());
                noteFolder.save();
            }
            return true;
        }
    }

    /**
     * 删除便签
     */
    public static void remove(Note note,boolean forever) {
        if(forever){
            // 删除便签的图片文件
            NoteAnalUtil.rmText(note.getContent());
            // 删除便签在便签夹下的目录项
            DataSupport.deleteAll(NoteFolder.class, "noteId = ?", String.valueOf(note.getId()));
            note.delete();
        }else{
            note.setRemoved(true);
            note.save();
        }
        sNoteList = null;
    }

    /**还原便签*/
    public static void restore(Note note){
        List<Folder> folders=DataSupport.where("id = (select folderId from NoteFolder " +
                "where noteId = ?)",String.valueOf(note.getId()))
                .find(Folder.class);
        if(!folders.isEmpty()){
            Folder f=folders.get(0);
            // 便签夹被标记为删除状态，恢复便签夹(此时是空的)
            if(f.isRemoved()){
                f.setRemoved(false);
                f.save();
            }
        }
        note.setRemoved(false);
        note.save();
        sNoteList=null;
        FolderDBHelper.clearCache();
    }

    /**
     * 当前便签夹的便签总数
     */
    public static int count() {
        if (sNoteList == null) {
            sNoteList = query(sFolderId);
        }
        return sNoteList.size();
    }

    /**
     * 便签在当前便签夹下的排名
     */
    public static int getRank(int id) {
        String sqlWhere = "";
        if (sOrder == Order.BY_CREATED_TIME || sOrder == Order.BY_MODIFIED_TIME) {
            sqlWhere = "isRemoved = ? and "+sOrder.toString() + " > (select " + sOrder.toString()
                    + " from Note where id = ?)";
        } else if (sOrder == Order.BY_CONTENT) {
            sqlWhere = "isRemoved = ? and "+sOrder.toString() + " < (select " + sOrder.toString()
                    + " from Note where id = ?)";
        }
        int rank;
        if (isRemoved||sFolderId < 0) {
            rank = DataSupport
                    .where(sqlWhere, isRemoved?"1":"0",String.valueOf(id))
                    .count(Note.class);
        } else {
            sqlWhere = "id in (select noteId from NoteFolder where folderId = ?) and "
                    + sqlWhere;
            rank = DataSupport
                    .where(sqlWhere, String.valueOf(sFolderId), "0",String.valueOf(id))
                    .count(Note.class);
        }
        return rank;
    }

    /**
     * 设置排名方式
     */
    public static void setsOrder(Order sOrder) {
        NoteDBHelper.sOrder = sOrder;
        sNoteList = null;
    }

    /**
     * 设置当前便签夹
     */
    public static void setsFolderId(int sFolderId) {
        NoteDBHelper.sFolderId = sFolderId;
        sNoteList = null;
    }

    /**
     * 设置查询过滤
     */
    public static void setFilter(boolean sFiltering, String sQueryText) {
        NoteDBHelper.sFiltering = sFiltering;
        NoteDBHelper.sQueryText = sQueryText;
        if (sFiltering && TextUtils.isEmpty(sQueryText)) {
            NoteDBHelper.sFiltering = false;
        }
        sNoteList = null;
    }


    /**
     * 根据便签夹id查询该便签夹下的所有便签
     */
    public static List<Note> query(int folderId) {
        if (isRemoved || folderId < 0) {
            if (sFiltering) {
                return DataSupport.where("isRemoved = ? and content like ?",
                        isRemoved?"1":"0", "%" + sQueryText + "%")
                        .order(sOrder.by()).find(Note.class);
            } else {
                return DataSupport
                        .where("isRemoved = ?", isRemoved?"1":"0")
                        .order(sOrder.by()).find(Note.class);
            }
        } else {
            if (sFiltering) {
                return DataSupport
                        .where("id in (select noteId from NoteFolder where folderId = ?)" +
                                        " and content like ? and isRemoved = 0",
                                String.valueOf(folderId), "%" + sQueryText + "%")
                        .order(sOrder.by())
                        .find(Note.class);
            } else {
                return DataSupport
                        .where("id in (select noteId from NoteFolder where folderId = ?)" +
                                        " and isRemoved = 0",
                                String.valueOf(folderId))
                        .order(sOrder.by())
                        .find(Note.class);
            }

        }
    }

    // FolderDBHelper.clearNotes调用
    static List<Note> query(int folderId,boolean removed,boolean filter) {
        boolean r=isRemoved;
        boolean f=sFiltering;
        isRemoved=removed;
        sFiltering=filter;
        List<Note> notes=query(folderId);
        isRemoved=r;
        sFiltering=f;
        return notes;
    }

    /**
     * 更新缓存
     */
    public static void query() {
        sNoteList = query(sFolderId);
    }

    public static void setType(int type) {
        if (type == GENERAL) {
            isRemoved = false;
        } else if (type == REMOVED) {
            isRemoved = true;
        } else {
            throw new IllegalArgumentException("type:" + type + " 参数不合法");
        }
        sType = type;
        sNoteList = null;
    }
}
