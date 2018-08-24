package com.example.lenovo.note.recy;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.lenovo.note.db.NoteDBUtil;

import java.util.HashSet;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class NoteAdapter extends RecyclerView.Adapter<MyViewHolder>
        implements Filterable{
    static int count=0;

    private NoteClickListener noteClickListener;
    private SelectCountsListener selectCountsListener;
    private boolean select=false;
    private boolean scroll=false;
    private Set<Integer> selectedSet=new HashSet<>();
    private int layoutType=MyViewHolderFactory.DEFAULT;
    private int width;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: "+count++);
        final MyViewHolder holder=MyViewHolderFactory.getType(parent,viewType);
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
        holder.bind(position,scroll,width);
        holder.setSelect(selectedSet.contains(position));
    }

    @Override
    public int getItemCount() {
        return NoteDBUtil.count();
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: 2018/7/27 多种布局
        return layoutType;
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycled();
        Log.d(TAG, "onViewRecycled: cancel task");
    }

    @Override
    public void onViewAttachedToWindow(MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "onViewAttachedToWindow: ");
    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d(TAG, "onViewDetachedFromWindow: ");
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                NoteDBUtil.setFilter(true,charSequence.toString());
                NoteDBUtil.query();
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notifyDataSetChanged();
            }
        };
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

    public boolean setLayoutType(int layoutType) {
        if(this.layoutType!=layoutType){
            this.layoutType = layoutType;
            return true;
        }
        return false;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public boolean isScroll() {
        return scroll;
    }


    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
