package com.example.lenovo.note;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.example.lenovo.note.util.BitmapUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/8/15.
 */

public class LoadThumbnailTask extends AsyncTask<Void,Integer,Boolean> {
    public interface OnCompleteListener{
        void onComplete(Bitmap bitmap);
    }

    String picName;
    int width;
    OnCompleteListener onCompleteListener;
    Bitmap bitmap;

    public LoadThumbnailTask(String picName, int width, OnCompleteListener onCompleteListener) {
        this.picName = picName;
        this.width = width;
        this.onCompleteListener = onCompleteListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(isCancelled()){
            return false;
        }
//        long time1=System.currentTimeMillis();
        bitmap= BitmapUtil.load(picName,width);
//        long time2=System.currentTimeMillis();
//        Log.d(TAG, "doInBackground: "+(time2-time1)+"ms");
        if(isCancelled()){
            return false;
        }

        BitmapUtil.putCache(picName,bitmap);
        bitmap=BitmapUtil.scaleTo(bitmap,width,1.0);
//        long time3=System.currentTimeMillis();
//        Log.d(TAG, "doInBackground: "+(time3-time2)+"ms");
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(onCompleteListener!=null){
            onCompleteListener.onComplete(bitmap);
        }
        bitmap=null;
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: "+picName);
        bitmap=null;
    }
}
