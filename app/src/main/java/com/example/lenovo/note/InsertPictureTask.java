package com.example.lenovo.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.example.lenovo.note.util.BitmapUtil;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/8/9.
 */

public class InsertPictureTask extends AsyncTask<Void,Integer,Boolean>{
    public interface InsertPictureListener{
        void onSuccess(Bitmap bitmap,String fileName);
        void onCanceled(String fileName);
    }

    private Context context;
    private int reqWidth;
    // FIXME: 2018/8/10 改为String
    private File file;
    private int srcType;
    private InsertPictureListener insertPictureListener;
    private Bitmap bitmap;
    private String fileName;

    public InsertPictureTask(Context context, int reqWidth, File file,
                             int srcType, InsertPictureListener insertPictureListener) {
        this.context = context;
        this.reqWidth = reqWidth;
        this.file = file;
        this.srcType = srcType;
        this.insertPictureListener = insertPictureListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        long time1=System.currentTimeMillis();  ///////////////////////

        bitmap= BitmapUtil.decodeFromFile(file.getAbsolutePath(),
                reqWidth,BitmapUtil.NO_REQUEST);

        long time2=System.currentTimeMillis();  ////////////////////
        Log.d(TAG, "doInBackground: "+(time2-time1)+"ms");
        
        if(srcType==NoteEditActivity.TAKE_PHOTO){
            file.delete();
        }
        fileName = System.currentTimeMillis() + ".png";
        if(isCancelled()){
            if(bitmap!=null) {
                bitmap.recycle();
                bitmap = null;
            }
            return false;
        }
        publishProgress();
        if(bitmap!=null) {
            BitmapUtil.save(context, bitmap, fileName);
        }

        long time3=System.currentTimeMillis();  ////////////////////////
        Log.d(TAG, "doInBackground: "+(time3-time2)+"ms");

        context=null;
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        bitmap=null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if(insertPictureListener!=null){
            insertPictureListener.onSuccess(bitmap,fileName);
            insertPictureListener=null;
        }
    }
}
