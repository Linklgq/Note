package com.example.lenovo.note;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.lenovo.note.db.FolderDBHelper;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder>
        implements Filterable{
    public interface OnItemClickListener {
        void onClick(int position);
    }

    public interface OnMenuItemClickListener{
        boolean onMenuItemClick(MenuItem item,int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView folderName;
        TextView noteCount;

        public ViewHolder(View itemView) {
            super(itemView);
            folderName=itemView.findViewById(R.id.folder_name);
            noteCount=itemView.findViewById(R.id.note_count);
        }
    }

    private MenuInflater menuInflater;
    private OnItemClickListener onItemClickListener;
    private OnMenuItemClickListener onMenuItemClickListener;

    public FolderAdapter(MenuInflater menuInflater,
                         OnItemClickListener onItemClickListener,
                         OnMenuItemClickListener onMenuItemClickListener) {
        this.menuInflater = menuInflater;
        this.onItemClickListener = onItemClickListener;
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener !=null){
                    onItemClickListener.onClick(holder.getAdapterPosition());
                }
            }
        });

        final PopupMenu popupMenu=new PopupMenu(parent.getContext(),holder.itemView);
        menuInflater.inflate(R.menu.folder_item_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(onMenuItemClickListener!=null){
                    return onMenuItemClickListener.onMenuItemClick(item,holder.getAdapterPosition());
                }
                return true;
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int pos=holder.getAdapterPosition();
                if(FolderDBHelper.getNoteCount(pos)==0){
                    popupMenu.getMenu().findItem(R.id.clear).setEnabled(false);
                }else{
                    popupMenu.getMenu().findItem(R.id.clear).setEnabled(true);
                }
                popupMenu.show();
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.folderName.setText(FolderDBHelper.get(position).getFolderName());
        holder.noteCount.setText(String.valueOf(FolderDBHelper.getNoteCount(position)));
    }

    @Override
    public int getItemCount() {
        return FolderDBHelper.folderCount();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FolderDBHelper.setFilter(true,charSequence.toString());
                FolderDBHelper.query();
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notifyDataSetChanged();
            }
        };
    }
}
