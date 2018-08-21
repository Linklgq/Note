package com.example.lenovo.note;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.note.db.FolderDBUtil;

/**
 * Created by Lenovo on 2018/8/20.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    public interface ItemOnClickListener{
        void onClick(int position);
        boolean onLongClick(int position);
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
    private ItemOnClickListener itemOnClickListener;

    public FolderAdapter(MenuInflater menuInflater, ItemOnClickListener itemOnClickListener) {
        this.menuInflater = menuInflater;
        this.itemOnClickListener = itemOnClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item,parent,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemOnClickListener!=null){
                    itemOnClickListener.onClick(holder.getAdapterPosition());
                }
            }
        });

        final PopupMenu popupMenu=new PopupMenu(parent.getContext(),holder.itemView);
        menuInflater.inflate(R.menu.folder_item_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.clear:{
                        Toast.makeText(MyApplication.getContext(), "clear",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.remove:{
                        Toast.makeText(MyApplication.getContext(), "remove",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.rename:{
                        Toast.makeText(MyApplication.getContext(), "rename",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                return true;
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                popupMenu.show();
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.folderName.setText(FolderDBUtil.get(position).getFolderName());
        holder.noteCount.setText(String.valueOf(FolderDBUtil.getNoteCount(position)));
    }

    @Override
    public int getItemCount() {
        return FolderDBUtil.folderCount();
    }
}
