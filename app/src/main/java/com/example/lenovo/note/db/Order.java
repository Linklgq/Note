package com.example.lenovo.note.db;

import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;

/**
 * Created by Lenovo on 2018/8/25.
 */

public enum Order{
    BY_CREATED_TIME("createdTime",R.string.by_created_time,0){
        @Override
        public String by() {
            return mAttrName+" desc";
        }
    },
    BY_MODIFIED_TIME("modifiedTime",R.string.by_modified_time,1){
        @Override
        public String by() {
            return mAttrName+" desc";
        }
    },
    BY_CONTENT("content",R.string.by_content,2){
        @Override
        public String by() {
            return mAttrName+",modifiedTime desc";
        }
    };

    final String mAttrName;
    public final String mTag;
    public final int mId;

    Order(String attrName,int resId,int id){
        mAttrName=attrName;
        mTag=MyApplication.getContext().getResources().getString(resId);
        mId=id;
    }

    public abstract String by();

    @Override
    public String toString() {
        return mAttrName;
    }

    public static Order findByTag(String tag){
        for(Order e:Order.values()){
            if(e.mTag.equals(tag)){
                return e;
            }
        }
        return BY_MODIFIED_TIME;
    }

    /** 保存现场使用id，tag跟布局相关，有可能被修改*/
    public static Order findById(int id){
        for(Order e:Order.values()){
            if(e.mId==id){
                return e;
            }
        }
        return BY_MODIFIED_TIME;
    }
}
