package com.example.lenovo.note;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.note.db.DBUtil;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.util.MyStringUtil;
import com.example.lenovo.note.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private boolean isSelect=false;
    private Set<Integer> selectedSet=new HashSet<>();
    private MainActivity mainActivity;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView keyword;
        TextView noteTitle;
        TextView noteContent;
        TextView updateTime;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            keyword=(TextView)itemView.findViewById(R.id.keyword);
            noteTitle=(TextView)itemView.findViewById(R.id.note_title);
            noteContent=(TextView)itemView.findViewById(R.id.note_content);
            updateTime=(TextView)itemView.findViewById(R.id.update_time);
            view=itemView;
        }

        public void setSelect(boolean select){
            if(select){
                view.setBackgroundColor(MyApplication.getContext()
                        .getResources().getColor(R.color.grayA));
            }else{
                view.setBackgroundColor(MyApplication.getContext()
                        .getResources().getColor(R.color.white));
            }
        }
    }

    public NoteAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item_linear,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                if(isSelect){
                    boolean b=!selectedSet.remove(position);
                    if(b){
                        selectedSet.add(position);
                    }
                    holder.setSelect(b);
                    mainActivity.setSelectedCounts(selectedSet.size());
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!isSelect){
                    setSelect(true);
                    selectedSet.add(holder.getAdapterPosition());
                    mainActivity.setSelect(true);
                    mainActivity.setSelectedCounts(selectedSet.size());
                    notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
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
        holder.setSelect(selectedSet.contains(position));
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

    public void setSelect(boolean select){
        isSelect=select;
        selectedSet.clear();
    }

    public void selectAll(){
        if(selectedSet.size()==getItemCount()){
            return;
        }
        selectedSet.clear();
        int n=getItemCount();
        for(int i=0;i<n;i++){
            selectedSet.add(i);
        }
        mainActivity.setSelectedCounts(selectedSet.size());
    }

    public void removeSelect(){
        List<Note> tList=new ArrayList<>();
        for(Integer integer:selectedSet){
            Log.d(TAG, "removeSelect: "+integer);
            tList.add(DBUtil.get(integer));
        }
        Toast.makeText(mainActivity, "delete "+tList.size(), Toast.LENGTH_SHORT).show();
        for(Note note:tList){
            DBUtil.remove(note);
        }
    }
}
