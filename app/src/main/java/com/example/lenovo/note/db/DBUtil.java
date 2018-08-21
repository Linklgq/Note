//package com.example.lenovo.note.db;
//
//import android.database.Cursor;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.example.lenovo.note.MyApplication;
//import com.example.lenovo.note.util.NoteAnalUtil;
//
//import org.litepal.crud.DataSupport;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.content.ContentValues.TAG;
//
///**
// * Created by Lenovo on 2018/7/26.
// */
//
//public class DBUtil {
//    public enum SortType{
//        BY_CREATED_TIME("createdTime"),
//        BY_MODIFIED_TIME("modifiedTime"),
//        BY_CONTENT("content");
//
//        String type;
//
//        SortType(String type){
//            this.type=type;
//        }
//
//        @Override
//        public String toString() {
//            return type;
//        }
//    }
//
//    /** 缓存*/
//    private static List<Note> noteList=new ArrayList<>();
//    private static final int SIZE=64;
//    private static int start=0;
//    private static SortType sortType= SortType.BY_MODIFIED_TIME;
//
//    public static Note get(int index){
//        if(index<start||index>=start+ noteList.size()){
//            start= index-SIZE/2>0?index-SIZE/2:0;
//            noteList = DataSupport.order(sortType+"desc").limit(SIZE).offset(start).find(Note.class);
//            Log.d(TAG, "get: "+noteList.size()+" start "+start);
//        }
//        return noteList.get(index-start);
//    }
//
//    public static Note findById(int id){
//        Note note=DataSupport.select("id").where("id = ?",String.valueOf(id))
//                .find(Note.class).get(0);
//        return note;
//    }
//
//    public static void modify(int index){
////        if(index<start||index>=start+ noteList.size()){
////            Toast.makeText(MyApplication.getContext(), "imposible out of index",
////                    Toast.LENGTH_SHORT).show();
////        }else{
//        noteList.clear();
//            if(NoteAnalUtil.trimWhiteChar(get(index).getContent()).isEmpty()){
//                remove(get(index));
//                Toast.makeText(MyApplication.getContext(),
//                        "空便签已自动删除", Toast.LENGTH_SHORT).show();
//                return;
//            }
////            noteList.get(index).save();
//            // TODO: 2018/7/26 滚动到修改便签
//            if(sortType!=SortType.BY_CREATED_TIME){
//                noteList.clear();
//            }
////        }
//    }
//
//    public static boolean modify(Note note){
//        noteList.clear();
//        if(NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()){
//            return false;
//        }else{
//            note.save();
//            return true;
//        }
//    }
//
//    public static boolean add(Note note){
//        if(NoteAnalUtil.trimWhiteChar(note.getContent()).isEmpty()){
//            Toast.makeText(MyApplication.getContext(),
//                    "空便签将不会保存", Toast.LENGTH_SHORT).show();
//            return false;
//        }else{
//            note.save();
//            noteList.clear();
//            return true;
//        }
//    }
//
//    public static void remove(Note note){
//        NoteAnalUtil.rmText(note.getContent());
//        note.delete();
//        noteList.clear();
//    }
//
//    public static void setSortType(SortType sortType) {
//        DBUtil.sortType = sortType;
//    }
//
//    public static int getCounts(){
//        return DataSupport.count(Note.class);
//    }
//
//    public static int getRank(int id,SortType type){
//        int rank=0;
//        String sql="";
//        if(type==SortType.BY_CREATED_TIME||type==SortType.BY_MODIFIED_TIME) {
//            sql="select count(id) from Note where "+type.toString()
//                    +">=(select "+type.toString()+" from Note where id="+id+")";
//            Cursor cursor = DataSupport.findBySQL(sql);
//            if(cursor.moveToFirst()){
//                rank=cursor.getInt(0);
//            }
//            cursor.close();
//        }
//        Log.d(TAG, "getRank: "+rank+" \n"+sql);
//        return rank;
//    }
//}
