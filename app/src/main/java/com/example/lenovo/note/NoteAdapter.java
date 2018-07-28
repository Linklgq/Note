package com.example.lenovo.note;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.util.MyStringUtil;
import com.example.lenovo.note.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView keyword;
        TextView noteTitle;
        TextView noteContent;
        TextView updateTime;

        public ViewHolder(View itemView) {
            super(itemView);
            keyword=(TextView)itemView.findViewById(R.id.keyword);
            noteTitle=(TextView)itemView.findViewById(R.id.note_title);
            noteContent=(TextView)itemView.findViewById(R.id.note_content);
            updateTime=(TextView)itemView.findViewById(R.id.update_time);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item_linear,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note=DBUtil.get(position);
        String str=note.getContent();
        holder.keyword.setText(MyStringUtil.getFirstChar(str));
        String title= StringUtils.substringBefore(str,"\n");
        holder.noteTitle.setText(title);
        String content=StringUtils.substringBefore(
                StringUtils.substringAfter(str,"\n"),"\n");
        holder.noteContent.setText(content);
        holder.updateTime.setText(TimeUtil.timeString(note.getModifiedTime()));
    }

    @Override
    public int getItemCount() {
        return DBUtil.getCounts();
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: 2018/7/27 多种布局
        return super.getItemViewType(position);
    }
}
