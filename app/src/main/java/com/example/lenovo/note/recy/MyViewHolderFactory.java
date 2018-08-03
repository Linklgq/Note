package com.example.lenovo.note.recy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.note.R;
import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.util.MyStringUtil;
import com.example.lenovo.note.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Lenovo on 2018/8/3.
 */

public class MyViewHolderFactory {
    public static final int DEFAULT=0;

    public static MyViewHolder newInstance(ViewGroup parent, int viewType){
        switch(viewType){
            case DEFAULT: {
                View view= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_item_default,parent,false);
                return new DefaultViewHolder(view);
            }
        }
        throw new IllegalArgumentException("'viewType'参数错误");
    }

    static class DefaultViewHolder extends MyViewHolder{
        TextView keyword;
        TextView noteTitle;
        TextView noteContent;
        TextView updateTime;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            keyword=(TextView)itemView.findViewById(R.id.keyword);
            noteTitle=(TextView)itemView.findViewById(R.id.note_title);
            noteContent=(TextView)itemView.findViewById(R.id.note_content);
            updateTime=(TextView)itemView.findViewById(R.id.update_time);
        }

        @Override
        void bind(int position) {
            Note note= DBUtil.get(position);
            String str=note.getContent();
            keyword.setText(MyStringUtil.getFirstChar(str));
            String title= StringUtils.substringBefore(str,"\n");
            noteTitle.setText(title);
            String content=StringUtils.substringBefore(
                    StringUtils.substringAfter(str,"\n"),"\n");
            noteContent.setText(content);
            updateTime.setText(TimeUtil.timeString(note.getModifiedTime()));
        }
    }
}
