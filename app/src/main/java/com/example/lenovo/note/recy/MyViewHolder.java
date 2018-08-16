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
        if(select){
//            itemView.setAlpha(0.3f);
//            itemView.setBackgroundColor(MyApplication.getContext()
//                    .getResources().getColor(R.color.grayA));
            itemView.setBackgroundColor(ContextCompat
                    .getColor(MyApplication.getContext(),R.color.grayA));
        }else{
//            itemView.setBackgroundColor(MyApplication.getContext()
//                    .getResources().getColor(R.color.white));
//            itemView.setAlpha(1.0f);
            itemView.setBackgroundColor(ContextCompat
                    .getColor(MyApplication.getContext(),R.color.white));
        }
    }

    void recycled(){}

    public void updateView(){}
}
