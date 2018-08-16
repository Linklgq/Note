package com.example.lenovo.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.example.lenovo.note.util.BitmapUtil;
import com.example.lenovo.note.util.NoteAnalUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/8/10.
 */

public class LoadNotePicTask extends AsyncTask<Void,LoadNotePicTask.Options,Boolean> {
    public interface LoadNotePicListener{
        void onMatch(Bitmap bitmap, int start, int end);
        void onCompleted();
    }

    public static class Options{
        Bitmap bitmap;
        int start;
        int end;

        public Options(Bitmap bitmap, int start, int end) {
            this.bitmap = bitmap;
            this.start = start;
            this.end = end;
        }
    }

    private CharSequence text;
    private Context context;
    private LoadNotePicListener loadNotePicListener;
    private int picWidth;

    public LoadNotePicTask(CharSequence text,LoadNotePicListener loadNotePicListener) {
        this.text = text;
        this.loadNotePicListener = loadNotePicListener;
        context=MyApplication.getContext();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(loadNotePicListener==null){
            return false;
        }
        long time1=System.currentTimeMillis();
        NoteAnalUtil.contentAnalyze(text,
                new NoteAnalUtil.MatchPictureListener(){
                    @Override
                    public void match(String picName, int start, int end) {
                        Bitmap bitmap= BitmapUtil.load(picName,picWidth);
                        publishProgress(new Options(bitmap,start,end));
                    }

                    @Override
                    public boolean cancel() {
                        return isCancelled();
                    }
                });
        long time2=System.currentTimeMillis();
        Log.d(TAG, "doInBackground: "+(time2-time1)+"ms");
        context=null;
        return true;
    }

    @Override
    protected void onProgressUpdate(Options... values) {
        Options options=values[0];
        if(loadNotePicListener!=null){
            loadNotePicListener.onMatch(options.bitmap,options.start,options.end);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        loadNotePicListener.onCompleted();
        loadNotePicListener=null;
    }

    @Override
    protected void onCancelled() {
        loadNotePicListener.onCompleted();
        loadNotePicListener=null;
    }

    public void setPicWidth(int picWidth) {
        this.picWidth = picWidth;
    }
}
