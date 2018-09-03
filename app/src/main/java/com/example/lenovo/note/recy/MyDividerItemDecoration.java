package com.example.lenovo.note.recy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by Lenovo on 2018/7/27.
 */

public class MyDividerItemDecoration extends DividerItemDecoration {
    public MyDividerItemDecoration(Context context, int orientation) {
        super(context, orientation);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(parent.getLayoutManager()instanceof LinearLayoutManager){
            outRect.set(0,8,0,8);
        }else if(parent.getLayoutManager()instanceof StaggeredGridLayoutManager){
            outRect.set( 12,12,12,12);
        }
//        outRect.set(12,12,12,12);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDraw(c, parent, state);
    }
}
