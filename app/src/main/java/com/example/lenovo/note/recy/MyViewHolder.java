package com.example.lenovo.note.recy;

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

    abstract void bind(int position);

    void setSelect(boolean select){
        if(select){
            itemView.setBackgroundColor(MyApplication.getContext()
                    .getResources().getColor(R.color.grayA));
        }else{
            itemView.setBackgroundColor(MyApplication.getContext()
                    .getResources().getColor(R.color.white));
        }
    }
}
