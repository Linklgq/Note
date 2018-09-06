package com.example.lenovo.note.recy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.note.LoadThumbnailTask;
import com.example.lenovo.note.MyApplication;
import com.example.lenovo.note.R;
import com.example.lenovo.note.db.Note;
import com.example.lenovo.note.db.NoteDBHelper;
import com.example.lenovo.note.util.BitmapUtil;
import com.example.lenovo.note.util.NoteAnalUtil;
import com.example.lenovo.note.util.TimeUtil;

/**
 * Created by Lenovo on 2018/8/3.
 */

public class MyViewHolderFactory {
    public static final int DEFAULT = 0;
    public static final int GRID = 1;

    public static MyViewHolder getType(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DEFAULT: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_item_linear, parent, false);
                return new DefaultViewHolder(view);
            }
            case GRID: {
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
            Note note = NoteDBHelper.get(position);
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
        }

        @Override
        public void bind(int position, final boolean scroll, int w) {
//            Log.d(TAG, "bind: " + count);
            width = (w - 168) / 2;
            // 低版本有多余的间距
            // 而且宽度比view宽度大的话会重复显示
            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT){
                width-=20;
            }
            if (width <= 0) {
                return;
            }

//            Log.d(TAG, "bind: width "+width);

            Note note = NoteDBHelper.get(position);
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
                    ImageSpan imageSpan = new ImageSpan(itemView.getContext(), bitmap);
                    spannable.setSpan(imageSpan, start, start+NoteAnalUtil.PIC_WORD.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                @Override
                public boolean cancel() {
                    return matchPic;
                }
            });

            noteContent.setText(spannable);
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
            }
        }
    }
}
