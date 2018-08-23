package com.example.lenovo.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.lenovo.note.db.FolderDBUtil;

/**
 * Created by Lenovo on 2018/8/22.
 */

public class FolderSpinnerAdapter extends BaseAdapter{
    @Override
    public int getCount() {
        return FolderDBUtil.folderCount()+1;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view= LayoutInflater.from(viewGroup.getContext()).inflate(
                android.R.layout.simple_spinner_dropdown_item,viewGroup,false);
        TextView textView=view.findViewById(android.R.id.text1);
        if(i==0){
            textView.setText("全部便签");
        }else{
            textView.setText(FolderDBUtil.get(i-1).getFolderName());
        }
        return view;
    }
}
