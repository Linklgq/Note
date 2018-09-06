package com.example.lenovo.note;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.lenovo.note.db.FolderDBHelper;

/**
 * Created by Lenovo on 2018/8/22.
 */

public class FolderSpinnerAdapter extends BaseAdapter{
    private static final String TAG = "FolderSpinnerAdapter";
    @Override
    public int getCount() {
//        Log.d(TAG, "getCount: "+FolderDBHelper.folderCount()+1);
        return FolderDBHelper.folderCount()+1;
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
//            Log.d(TAG, "getView: "+(i-1));
            textView.setText(FolderDBHelper.get(i-1).getFolderName());
        }
        return view;
    }
}
