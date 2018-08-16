package com.example.lenovo.note.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import static android.content.ContentValues.TAG;
import static android.graphics.BitmapFactory.decodeFile;
import static android.graphics.BitmapFactory.decodeResource;

/**
 * Created by Lenovo on 2018/8/7.
 */

public class BitmapUtil {
    public static final int NO_REQUEST=0;
    public static final int WIDTH_AND_HEIGHT=1;
    public static final int FOLLOW_WIDTH=2;
    public static final int FOLLOW_HEIGHT=3;

    private static Bitmap failed;
    private static final int FAILED_ID =R.drawable.failed_picture;
    private static final int DEFAULT_WIDTH=300;

    static class BitmapNode{
        String name;
        Bitmap data;

        public BitmapNode(String name, Bitmap data) {
            this.name = name;
            this.data = data;
        }
    }

    private static int sCachedSize=16;
    private static int sCachedWidth=256;
    private static final List<BitmapNode> mCachedBitmaps=new Vector<>(sCachedSize);

    private static Bitmap loading;
    private static final int LOADING_ID=R.drawable.loading;

    public static Bitmap getFailed(){
        if(failed==null){
            failed=decodeFromResource(FAILED_ID,DEFAULT_WIDTH);
        }
        return failed;
    }

    public static Bitmap getLoading(int width){
        if(loading==null||loading.getWidth()!=width){
            loading=decodeFromResource(LOADING_ID,width);
        }
        return loading;
    }

    public static Bitmap cachedBitmap(String name){
        if(name==null){
            return null;
        }
        Bitmap bitmap=null;
        for(int i=mCachedBitmaps.size()-1;i>=0;i--){
            if(name.equals(mCachedBitmaps.get(i).name)){
                bitmap=mCachedBitmaps.get(i).data;
                Log.d(TAG, "cachedBitmap: get cache "+name);
                break;
            }
        }
        return bitmap;
    }

    public static void addBitmapToCache(String name,Bitmap data){
        if(name==null){
            return;
        }
        // 如果已缓存，将其移到队尾
        for(int i=mCachedBitmaps.size()-1;i>=0;i--){
            if(name.equals(mCachedBitmaps.get(i).name)){
                BitmapNode node=mCachedBitmaps.get(i);
                mCachedBitmaps.remove(i);
                mCachedBitmaps.add(node);
                return;
            }
        }
        // 如果队列已达最大长度，移除队首元素
        if(mCachedBitmaps.size()==sCachedSize){
            mCachedBitmaps.remove(0);
        }

        data=scaleTo(data,sCachedWidth,-1.0);
        mCachedBitmaps.add(new BitmapNode(name,data));
        Log.d(TAG, "addBitmapToCache: "+name);
    }

    public static Bitmap decodeFromResource(int id, int reqWidth){
        if(reqWidth<=0){
            reqWidth=NO_REQUEST;
        }
        Resources res= MyApplication.getContext().getResources();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeResource(res, id, options);
        options.inSampleSize = calculateSampleSize(options,reqWidth,0);
        options.inJustDecodeBounds =false;
        Bitmap temp=BitmapFactory.decodeResource(res,id,options);
        if(temp==null){
            return null;
        }
        Log.d(TAG, "decodeFromResource: "+temp.getWidth());
        Bitmap result=scaleTo(temp,reqWidth,-1.0);
        Log.d(TAG, "decodeFromResource: "+result.getWidth());
//        temp.recycle();
        return result;
    }

    @Nullable
    public static Bitmap decodeFromFile(String filePath, int reqWidth){
        if(reqWidth<=0){
            return BitmapFactory.decodeFile(filePath);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(filePath,options);
        options.inSampleSize = calculateSampleSize(options,reqWidth,0);
        options.inJustDecodeBounds =false;
        Bitmap temp=BitmapFactory.decodeFile(filePath,options);
        if(temp==null){
            return null;
        }
        Log.d(TAG, "decodeFromFile: "+temp.getWidth()+" "+temp.getHeight());
        Bitmap result=scaleTo(temp,reqWidth,-1.0);
        Log.d(TAG, "decodeFromFile: "+result.getWidth()+" "+result.getHeight());

//        temp.recycle();
        return result;
    }

    public static BitmapFactory.Options measureFromFile(String filePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);
        return options;
    }

//    public static Bitmap decodeFromStream(InputStream inputStream,int reqWidth){
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(inputStream,null,options);
//        options.inSampleSize = calculateSampleSize(options,reqWidth,0);
//        options.inJustDecodeBounds =false;
//        Bitmap temp=BitmapFactory.decodeStream(inputStream,null,options);
//        Bitmap result=scaleTo(temp,reqWidth);
//        temp.recycle();
//        return result;
//    }

    public static Bitmap scaleTo(Bitmap oldBitmap,int afterWidth,double maxProp){
        int width=oldBitmap.getWidth();
        int height;
        if(maxProp>0) {
            height=oldBitmap.getHeight() < oldBitmap.getWidth() * maxProp ? oldBitmap.getHeight()
                    : (int) (oldBitmap.getWidth() * maxProp);
        }else{
            height=oldBitmap.getHeight();
        }
        Matrix matrix=new Matrix();
        matrix.postScale(afterWidth*1.0f/width,
                    afterWidth*1.0f/width);
        Bitmap newBitmap = Bitmap.createBitmap(oldBitmap,
                0, 0, width,height, matrix, true);
        return newBitmap;
    }

    public static boolean save(Bitmap bitmap,String fileName){
        Context context=MyApplication.getContext();
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

    public static Bitmap load(String fileName,int reqWidth){
//        if(reqWidth<=0){
//            reqWidth=NO_REQUEST;
//        }
        Context context=MyApplication.getContext();
        String filePath=context.getFilesDir().getAbsolutePath()+"/"+fileName;

        Bitmap bitmap=decodeFromFile(filePath,reqWidth);
        if(bitmap==null){
            Log.d(TAG, "load: getfailed "+filePath);
            return getFailed();
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
