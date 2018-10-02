package com.example.lenovo.note.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.BitmapFactory.decodeResource;

/**
 * Created by Lenovo on 2018/8/7.
 */

public class BitmapUtil {
    public static final int NO_REQUEST=0;

    public static class BitmapCache extends LruCache<String,Bitmap>{
        public BitmapCache(int maxSize) {
            super(maxSize);
//            Log.d(TAG, "BitmapCache: "+maxSize/1024/1024+"MB");
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
//            Log.d(TAG, "sizeOf: "+value.getByteCount()/1024+"KB");
            return value.getByteCount();
        }
    }

    private static Bitmap failed;
    private static final int FAILED_ID =R.drawable.failed_picture;
    private static final int DEFAULT_WIDTH=300;

    private static Bitmap loading;
    private static final int LOADING_ID=R.drawable.loading_bg;

    private static int sCachedWidth=256;
    private static final int DEFAULT_CACHE_SIZE=(int)(Runtime.getRuntime().maxMemory()/16);
    private static final BitmapCache BITMAP_CACHE =new BitmapCache(DEFAULT_CACHE_SIZE);

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

    /** 获取图片缓存*/
    public static Bitmap getCache(String key){
        if(key==null){
            return null;
        }
        return BITMAP_CACHE.get(key);
    }

    /** 存入缓存*/
    public static void putCache(String key,Bitmap value){
        if(key==null||value==null){
            return;
        }
        value=scaleTo(value,sCachedWidth,-1.0);
        BITMAP_CACHE.put(key,value);
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
        Bitmap result=scaleTo(temp,reqWidth,-1.0);
        return result;
    }

    @Nullable
    public static Bitmap decodeFromFile(String filePath, int reqWidth){
        int degree=getBitmapDegree(filePath);
        if(reqWidth<=0){
            Bitmap result=BitmapFactory.decodeFile(filePath);
            if(degree!=0){
                result=rotateBitmapByDegree(result,degree);
            }
            return result;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig= Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);
        if(degree==0||degree==180){
            options.inSampleSize = calculateSampleSize(options,reqWidth,0);
        }else{
            options.inSampleSize = calculateSampleSize(options,0,reqWidth);
        }
        options.inJustDecodeBounds =false;
        Bitmap temp=BitmapFactory.decodeFile(filePath,options);
        if(temp==null){
            return null;
        }
        if(degree!=0){
            temp=rotateBitmapByDegree(temp,degree);
        }
        Bitmap result=scaleTo(temp,reqWidth,-1.0);
        return result;
    }

    /** 测量图片的宽高*/
    public static BitmapFactory.Options measureFromFile(String filePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);
        return options;
    }

    /** 对图片进行缩放，缩放后可能还是是同一个对象
     * maxProp为高与宽的最大比值*/
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

    /** 保存到包名/files/目录下 */
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

    /** 从包名/files/目录下加载图片 */
    public static Bitmap load(String fileName,int reqWidth){
//        if(reqWidth<=0){
//            reqWidth=NO_REQUEST;
//        }
        Context context=MyApplication.getContext();
        String filePath=context.getFilesDir().getAbsolutePath()+"/"+fileName;

        Bitmap bitmap=decodeFromFile(filePath,reqWidth);
        if(bitmap==null){
            return getFailed();
        }
        return bitmap;
    }

    /** 计算合适的采样率，以期降低图片占用内存 */
    private static int calculateSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int width = options.outWidth;
        int height =options.outHeight;
        int inSampleSize = 1;
        int halfWidth = width/2;
        int halfHeight = height/2;
        while((halfWidth/inSampleSize)>=reqWidth&& (halfHeight/inSampleSize)>=reqHeight){
            inSampleSize*=2;
        }
        return inSampleSize;
    }

    private static int getBitmapDegree(String filePath){
        int degree=0;
        try {
            ExifInterface exifInterface=new ExifInterface(filePath);
            int orientation=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch(orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private static Bitmap rotateBitmapByDegree(Bitmap bitmap,int degree){
        Bitmap result=null;
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        result=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),
                matrix,true);
        if(result==null){
            result=bitmap;
        }
        if(bitmap!=result){
            bitmap.recycle();
        }
        return result;
    }
}
