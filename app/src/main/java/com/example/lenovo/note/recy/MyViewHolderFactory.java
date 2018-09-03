package com.example.lenovo.note.recy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.note.LoadThumbnailTask;
import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.db.NoteDBUtil;
import com.example.lenovo.note.util.BitmapUtil;
import com.example.lenovo.note.util.NoteAnalUtil;
import com.example.lenovo.note.util.TimeUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/8/3.
 */

public class MyViewHolderFactory {
    public static final int DEFAULT = 0;
    public static final int GRID = 1;

    public static MyViewHolder getType(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DEFAULT: {
                Log.d(TAG, "getType: default");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_item_linear, parent, false);
                return new DefaultViewHolder(view);
            }
            case GRID: {
                Log.d(TAG, "getType: grid");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_item_grid, parent, false);
                return new GridViewHolder(view);
            }
        }
        throw new IllegalArgumentException("'viewType'参数错误");
    }

    static class DefaultViewHolder extends MyViewHolder {
        TextView keyword;
        TextView noteTitle;
        TextView noteContent;
        TextView updateTime;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            keyword = (TextView) itemView.findViewById(R.id.keyword);
            noteTitle = (TextView) itemView.findViewById(R.id.note_title);
            noteContent = (TextView) itemView.findViewById(R.id.note_content);
            updateTime = (TextView) itemView.findViewById(R.id.update_time);
        }

        @Override
        public void bind(int position, boolean scroll, int w) {
            Note note = NoteDBUtil.get(position);
            String str = NoteAnalUtil.trimWhiteChar(note.getContent());
            keyword.setText(NoteAnalUtil.firstWorld(str));
            noteTitle.setText(NoteAnalUtil.contentToString(NoteAnalUtil.paragraph(str, 0)));
            noteContent.setText(NoteAnalUtil.contentToString(NoteAnalUtil.paragraph(str, 1)));
            updateTime.setText(TimeUtil.timeString(note.getModifiedTime()));
        }
    }

    static class GridViewHolder extends MyViewHolder {
        static int nums = 0;

        int count;
        TextView noteContent;
        TextView updateTime;

        boolean matchPic=false;
        boolean havePic=false;
        String fileName;
        int begin;
        String text;
        int width;
        LoadThumbnailTask task;

        public GridViewHolder(final View itemView) {
            super(itemView);
            noteContent = (TextView) itemView.findViewById(R.id.note_content);
            updateTime = (TextView) itemView.findViewById(R.id.update_time);
            nums++;
            count = nums;

            noteContent.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: width "+noteContent.getWidth());
                }
            });
        }

//        @Override
//        public void bind(final int position, final boolean scroll, int w) {
//            Log.d(TAG, "bind: " + count);
//            long time1=System.currentTimeMillis();
//            final int width = (w - 144) / 2;
//            if (width <= 0) {
//                return;
//            }
//            Note note = DBUtil.get(position);
//            updateTime.setText(TimeUtil.timeString(note.getModifiedTime()));
//            String text = NoteAnalUtil.rmStartWhiteChar(note.getContent());
//            final Spannable spannable = new SpannableString(NoteAnalUtil.contentToString(text));
//            havePic = false;
//            NoteAnalUtil.contentAnalyze(text, new NoteAnalUtil.MatchPictureListener() {
//                @Override
//                public void match(String picName, final int start, int end) {
//                    Bitmap bitmap=BitmapUtil.cachedBitmap(picName);
//                    if(bitmap==null){
//
//                        bitmap=BitmapUtil.getLoading(width);
//                        BitmapFactory.Options options = BitmapUtil.measureFromFile(
//                                MyApplication.getContext().getFilesDir().getAbsolutePath()
//                                        + "/" + picName);
//                        bitmap = BitmapUtil.scaleTo(bitmap, width, options.outHeight * 1.0
//                                / options.outWidth);
//
//                        if (!scroll) {
//                            task = new LoadThumbnailTask(picName, width,
//                                    new LoadThumbnailTask.OnCompleteListener() {
//                                        @Override
//                                        public void onComplete(Bitmap bitmap) {
//                                            ImageSpan imageSpan = new ImageSpan(itemView.getContext(), bitmap);
//                                            spannable.setSpan(imageSpan, start, NoteAnalUtil.PIC_WORD.length(),
//                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                            noteContent.setText(spannable);
//                                        }
//                                    });
//                            task.execute();
//                        }
//                    }else{
//                        bitmap=BitmapUtil.scaleTo(bitmap, width, 1.0);
//                    }
//                    ImageSpan imageSpan = new ImageSpan(itemView.getContext(), bitmap);
//                    spannable.setSpan(imageSpan, start, NoteAnalUtil.PIC_WORD.length(),
//                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    havePic = true;
//                }
//
//                @Override
//                public boolean cancel() {
//                    return havePic;
//                }
//            });
//
//            noteContent.setText(spannable);
//
//            long time2=System.currentTimeMillis();
//            Log.d(TAG, "match: scroll load "+(time2-time1)+"ms");
//        }

        @Override
        public void bind(int position, final boolean scroll, int w) {
            Log.d(TAG, "bind: " + count);
            long time1=System.currentTimeMillis();
            // FIXME: 2018/8/24 精准宽度
            width = (w - 168) / 2;
            // 低版本有多余的间距
            // 而且宽度比view宽度大的话会重复显示
            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT){
                width-=20;
            }
            if (width <= 0) {
                return;
            }

            Log.d(TAG, "bind: width "+width);

            Note note = NoteDBUtil.get(position);
            updateTime.setText(TimeUtil.timeString(note.getModifiedTime()));
            String content = NoteAnalUtil.trimWhiteChar(note.getContent());
            text=NoteAnalUtil.contentToString(content);
            final Spannable spannable = new SpannableString(text);
            matchPic=false;
            NoteAnalUtil.contentAnalyze(content, new NoteAnalUtil.MatchPictureListener() {
                @Override
                public void match(String picName, int start, int end) {
                    fileName=picName;
                    begin=start;
                    matchPic=true;
                    havePic=true;
                    Log.d(TAG, "match: "+text+" "+fileName);
                    Bitmap bitmap=BitmapUtil.getCache(picName);
                    if(bitmap==null){
                        bitmap=BitmapUtil.getLoading(width);
                        BitmapFactory.Options options = BitmapUtil.measureFromFile(
                                MyApplication.getContext().getFilesDir().getAbsolutePath()
                                        + "/" + picName);
                        bitmap = BitmapUtil.scaleTo(bitmap, width, options.outHeight * 1.0
                                / options.outWidth);

                        if (!scroll) {
                            execTask();
                        }
                    }else{
                        bitmap=BitmapUtil.scaleTo(bitmap, width, 1.0);
                        havePic=false;
                    }
                    Log.d(TAG, "match: spannable "+spannable+" "+start);
                    ImageSpan imageSpan = new ImageSpan(itemView.getContext(), bitmap);
                    spannable.setSpan(imageSpan, start, start+NoteAnalUtil.PIC_WORD.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    Log.d(TAG, "match: pic"+text+(bitmap==null));
                }

                @Override
                public boolean cancel() {
                    return matchPic;
                }
            });

            noteContent.setText(spannable);

            long time2=System.currentTimeMillis();
            Log.d(TAG, "match: scroll load "+(time2-time1)+"ms");
        }

        void cancelTask(){
            if(task!=null&&task.getStatus()== AsyncTask.Status.RUNNING){
                task.cancel(true);
            }
        }

        private void execTask(){
            cancelTask();
            task = new LoadThumbnailTask(fileName, width,
                    new LoadThumbnailTask.OnCompleteListener() {
                        @Override
                        public void onComplete(Bitmap bitmap) {
                            if(bitmap==null){
                                Log.d(TAG, "onComplete: bitmap is null");
                            }
                            if(!havePic){
                                return;
                            }
                            Spannable spannable=new SpannableString(text);
                            ImageSpan imageSpan = new ImageSpan(itemView.getContext(), bitmap);
                            spannable.setSpan(imageSpan, begin, begin+NoteAnalUtil.PIC_WORD.length(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            noteContent.setText(spannable);

                            havePic=false;
                        }
                    });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        void recycled() {
            cancelTask();
            havePic=false;
        }

        @Override
        public void updateView() {
            if(havePic){
                execTask();
                Log.d(TAG, "updateView: have pic");
            }
        }
    }
}
