package com.example.lenovo.note.recy;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;

/**
 * Created by Lenovo on 2018/8/3.
 */

public abstract class MyViewHolder extends RecyclerView.ViewHolder {
    public MyViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(int position,boolean scroll,int w);

    void setSelect(boolean select){
        // FIXME: 2018/9/6 使用view的选择接口
        if(select){
            itemView.setBackgroundColor(ContextCompat
                    .getColor(MyApplication.getContext(), R.color.grayC));
        }else{
            itemView.setBackgroundColor(ContextCompat
                    .getColor(MyApplication.getContext(),R.color.white));
        }
    }

    void recycled(){}

    public void updateView(){}
}
