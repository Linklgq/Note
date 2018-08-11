package com.example.lenovo.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Spannable;
import android.util.Log;

import com.example.lenovo.note.util.NoteAnalUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/8/10.
 */

public class LoadNotePicTask extends AsyncTask<Void,LoadNotePicTask.Options,Boolean> {
    public interface LoadNotePicListener{
        void onMatch(Bitmap bitmap, int start, int end);
        void onCompleted(Spannable spannable);
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
    Spannable spannable;

    public LoadNotePicTask(CharSequence text, Context context, LoadNotePicListener loadNotePicListener) {
        this.text = text;
        this.context = context;
        this.loadNotePicListener = loadNotePicListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if(loadNotePicListener==null){
            return false;
        }
        long time1=System.currentTimeMillis();
        spannable=NoteAnalUtil.contentAnalyze(text,context,
                new NoteAnalUtil.MatchPictureListener(){
                    @Override
                    public void match(Bitmap bitmap, int start, int end) {
                        publishProgress(new Options(bitmap,start,end));
                    }

                    @Override
                    public boolean cancel() {
                        return isCancelled();
                    }

                    @Override
                    public Bitmap customBitmap() {
                        return null;
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
        loadNotePicListener.onCompleted(spannable);
        loadNotePicListener=null;
        spannable=null;
    }

    @Override
    protected void onCancelled() {
        loadNotePicListener.onCompleted(spannable);
        loadNotePicListener=null;
        spannable=null;
    }
}
