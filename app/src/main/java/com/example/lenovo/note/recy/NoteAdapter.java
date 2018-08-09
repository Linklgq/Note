package com.example.lenovo.note.recy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.lenovo.note.db.DBUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class NoteAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private NoteClickListener noteClickListener;
    private SelectCountsListener selectCountsListener;
    private boolean select=false;
    private Set<Integer> selectedSet=new HashSet<>();

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder=MyViewHolderFactory.newInstance(parent,viewType);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noteClickListener !=null) {
                    noteClickListener.onClick(holder);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(noteClickListener !=null) {
                    return noteClickListener.onLongClick(holder);
                }
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(position);
        holder.setSelect(selectedSet.contains(position));
    }

    @Override
    public int getItemCount() {
        return DBUtil.getCounts();
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: 2018/7/27 多种布局
        return MyViewHolderFactory.DEFAULT;
    }

    public void setNoteClickListener(NoteClickListener noteClickListener) {
        this.noteClickListener = noteClickListener;
    }

    public void setSelectCountsListener(SelectCountsListener selectCountsListener) {
        this.selectCountsListener = selectCountsListener;
    }

    public void setSelect(boolean select){
        this.select=select;
        selectedSet.clear();
    }

    public void select(MyViewHolder holder){
        if(!select){
            setSelect(true);
        }
        Integer i=holder.getAdapterPosition();
        if(!selectedSet.remove(i)){
            selectedSet.add(i);
            holder.setSelect(true);
        }else{
            holder.setSelect(false);
        }

        if(selectCountsListener!=null){
            selectCountsListener.setCounts(selectedSet.size());
        }
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

        if(selectCountsListener!=null){
            selectCountsListener.setCounts(selectedSet.size());
        }
    }

    public Set<Integer> getSelectedSet() {
        return selectedSet;
    }
}
