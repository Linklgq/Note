package com.example.lenovo.note.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import com.example.lenovo.note.MyApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/7/28.
 */

public class NoteAnalUtil {
    public interface MatchPictureListener {
        void match(String picName, int start, int end);
        boolean cancel();
    }

    public static final String PIC_KEYWORLD = "图";
    public static final String PIC_WORD = "[图片]";
    public static final String PIC_NAME = "\\d+.png";
    public static final String PIC_TAG = "<img src=\"" + PIC_NAME + "\">";

    public static Spannable firstWorld(String str) {
        SpannableString res;
        Matcher matcher = Pattern.compile("^" + PIC_TAG).matcher(str);
        if (matcher.find() && matcher.start() == 0) {
            res = new SpannableString(PIC_KEYWORLD);
        } else {
            char c = str.charAt(0);
            if (c >= 'a' && c <= 'z') {
                c = (char) (c - 'a' + 'A');
            }
            res = new SpannableString(String.valueOf(c));
        }
        return res;
    }

    public static String contentToString(String str) {
        String str1 = str.replaceAll(PIC_TAG, PIC_WORD);
        return str1;
    }

    public static void contentAnalyze(CharSequence text,
                                           MatchPictureListener matchPictureListener) {
//        Spannable spannable = new SpannableString(text);
        Matcher matcher = Pattern.compile(PIC_TAG).matcher(text);
        while (matcher.find()) {
            if (matchPictureListener != null && matchPictureListener.cancel()) {
                break;
            }
            Matcher m = Pattern.compile(PIC_NAME).matcher(matcher.group());
            if (m.find()) {
//                Bitmap bitmap;
//                if (matchPictureListener == null || matchPictureListener.customBitmap() == null) {
//                    long time1=System.currentTimeMillis();
//                    bitmap = BitmapUtil.load(m.group());
//                    long time2=System.currentTimeMillis();
//                    Log.d(TAG, "contentAnalyze: "+(time2-time1)+"ms");
//                } else {
//                    bitmap = matchPictureListener.customBitmap();
//                }
//                ImageSpan imageSpan = new ImageSpan(context, bitmap);
//                spannable.setSpan(imageSpan, matcher.start(), matcher.end(),
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (matchPictureListener != null) {
                    matchPictureListener.match(m.group(), matcher.start(), matcher.end());
                }
            }
        }
    }

    public static String paragraph(String text, int index) {
        String str;
        String[] array = text.split("\n", index + 2);
        Log.d(TAG, "paragraph: " + array.length);
        if (index < array.length) {
            str = array[index];
        } else {
            str = "";
        }
        return str;
    }

    @NonNull
    public static String trimWhiteChar(String str) {
        int i,j;
        for (i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                break;
            }
        }
        for(j=str.length()-1;j>=0&&j>i;j--){
            if (!Character.isWhitespace(str.charAt(j))) {
                break;
            }
        }
        return str.substring(i,j+1);
    }

    public static void rmText(CharSequence charSequence) {
        Context context= MyApplication.getContext();
        Matcher matcher = Pattern.compile(PIC_TAG).matcher(charSequence);
        // 删除图片文件
        while (matcher.find()) {
            Matcher m = Pattern.compile(PIC_NAME).matcher(matcher.group());
            if (m.find()) {
                context.deleteFile(m.group());
//                Toast.makeText(context, "删除图片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
