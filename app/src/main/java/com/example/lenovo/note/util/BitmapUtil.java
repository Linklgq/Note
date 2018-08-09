package com.example.lenovo.note.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by Lenovo on 2018/8/7.
 */

public class BitmapUtil {
    public static final int NO_REQUEST=0;
    public static final int WIDTH_AND_HEIGHT=1;
    public static final int FOLLOW_WIDTH=2;
    public static final int FOLLOW_HEIGHT=3;

//    public static Bitmap decodeFromResource(Resources res,int id,int reqWidth,int reqHeight){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, id, options);
//        options.inSampleSize = calculateSampleSize(options,reqWidth,reqHeight);
//        options.inJustDecodeBounds =false;
//        return  BitmapFactory.decodeResource(res,id,options);
//    }

    public static Bitmap decodeFromFile(String filePath,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(filePath,options);
        options.inSampleSize = calculateSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds =false;
        Bitmap temp=BitmapFactory.decodeFile(filePath,options);
        Bitmap res=scaleTo(temp,reqWidth,0,FOLLOW_WIDTH);
        temp.recycle();
        return res;
    }

    private static Bitmap scaleTo(Bitmap oldBitmap,int afterWidth,int afterHeight,int scaleType){
        Matrix matrix=new Matrix();
        if(scaleType==WIDTH_AND_HEIGHT){
            matrix.postScale(afterWidth*1.0f/oldBitmap.getWidth(),
                    afterHeight*1.0f/oldBitmap.getHeight());
        }else if(scaleType==FOLLOW_WIDTH){
            matrix.postScale(afterWidth*1.0f/oldBitmap.getWidth(),
                    afterWidth*1.0f/oldBitmap.getWidth());
        }else if(scaleType==FOLLOW_HEIGHT){
            matrix.postScale(afterHeight*1.0f/oldBitmap.getHeight(),
                    afterHeight*1.0f/oldBitmap.getHeight());
        }
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap,
                0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
//        oldBitmap.recycle();
        return newBitmap;
    }

    public static boolean save(Context context,Bitmap bitmap,String fileName){
        FileOutputStream out=null;
        try{
            out=context.openFileOutput(fileName,Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,out);
            out.flush();
            return true;
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try {
                if(out!=null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Bitmap load(Context context,String fileName){
        FileInputStream in=null;
        Bitmap bitmap=null;
        try{
            in=context.openFileInput(fileName);
            bitmap=BitmapFactory.decodeStream(in);

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }finally {
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private static int calculateSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int width = options.outWidth;
        int height =options.outHeight;
        int inSampleSize = 1;
        int halfWidth = width/2;
        int halfHeight = height/2;
//        Log.d(TAG, "calculateSampleSize: "+reqWidth+" "+reqHeight);
        while((halfWidth/inSampleSize)>=reqWidth&& (halfHeight/inSampleSize)>=reqHeight){
            inSampleSize*=2;
        }
        return inSampleSize;
    }
}
