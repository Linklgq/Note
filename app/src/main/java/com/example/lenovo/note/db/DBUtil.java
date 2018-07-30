package com.example.lenovo.note.db;

import android.util.Log;
import android.widget.Toast;

import com.example.lenovo.note.MyApplication;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.example.lenovo.note.db.DBUtil.SortType.BY_MODIFIED_TIME;

/**
 * Created by Lenovo on 2018/7/26.
 */

public class DBUtil {
//    public static final int BY_CREATED_TIME=0;
//    public static final int BY_MODIFIED_TIME=1;
//    public static final int BY_CONTENT=2;
    public enum SortType{
        BY_CREATED_TIME("createdTime"),
        BY_MODIFIED_TIME("modifiedTime"),
        BY_CONTENT("content");

        String type;

        SortType(String type){
            this.type=type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private static List<Note> noteList=new ArrayList<>();
    private static final int SIZE=64;
    private static int start=0;
    private static SortType sortType= BY_MODIFIED_TIME;

    public static Note get(int index){
        if(index<start||index>=start+ noteList.size()){
            start= index-SIZE/2>0?index-SIZE/2:0;
            noteList = DataSupport.order(sortType+"desc").limit(SIZE).offset(start).find(Note.class);
            Log.d(TAG, "get: "+noteList.size()+" start "+start);
        }
        return noteList.get(index-start);
    }

    public static void modify(int index){
        if(index<start||index>=start+ noteList.size()){
            Toast.makeText(MyApplication.getContext(), "imposible out of index",
                    Toast.LENGTH_SHORT).show();
        }else{
            if(!noteList.get(index).getContent().trim().isEmpty()){
                remove(noteList.get(index));
                Toast.makeText(MyApplication.getContext(),
                        "空便签已自动删除", Toast.LENGTH_SHORT).show();
                return;
            }
            noteList.get(index).save();
            // TODO: 2018/7/26 滚动到修改便签
            if(sortType!=SortType.BY_CREATED_TIME){
                noteList.clear();
            }
        }
    }

    public static void add(Note note){
        if(!note.getContent().trim().isEmpty()){
            note.save();
            // TODO: 2018/7/26  滚动到新便签
            noteList.clear();
        }else{
            Toast.makeText(MyApplication.getContext(),
                    "空便签将不会保存", Toast.LENGTH_SHORT).show();
        }
    }

    public static void remove(Note note){
        // TODO: 2018/7/26 回收站
        note.delete();
        noteList.clear();
        Log.d(TAG, "remove: 1"+".have "+getCounts());
    }

    public static void setSortType(SortType sortType) {
        DBUtil.sortType = sortType;
    }

    public static int getCounts(){
        return DataSupport.count(Note.class);
    }
}
