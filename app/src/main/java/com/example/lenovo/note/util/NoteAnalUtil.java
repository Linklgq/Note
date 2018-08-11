package com.example.lenovo.note.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 2018/7/28.
 */

public class NoteAnalUtil {
    public interface MatchPictureListener {
        void match(Bitmap bitmap, int start, int end);

        boolean cancel();

        Bitmap customBitmap();
    }

    private static final String PIC_KEYWORLD = "图";
    private static final String PIC_WORD = "[图片]";
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

    public static Spannable contentToString(String str) {
        String str1 = str.replaceAll(PIC_TAG, PIC_WORD);
        return new SpannableString(str1);
    }

    public static Spannable contentAnalyze(CharSequence text, Context context,
                                           MatchPictureListener matchPictureListener) {
        Spannable spannable = new SpannableString(text);
        Matcher matcher = Pattern.compile(PIC_TAG).matcher(text);
        while (matcher.find()) {
            if (matchPictureListener != null && matchPictureListener.cancel()) {
                break;
            }
            Matcher m = Pattern.compile(PIC_NAME).matcher(matcher.group());
            if (m.find()) {
                Bitmap bitmap;
                if (matchPictureListener == null || matchPictureListener.customBitmap() == null) {
                    long time1=System.currentTimeMillis();
                    bitmap = BitmapUtil.load(context, m.group());
                    long time2=System.currentTimeMillis();
                    Log.d(TAG, "contentAnalyze: "+(time2-time1)+"ms");
                } else {
                    bitmap = matchPictureListener.customBitmap();
                }
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannable.setSpan(imageSpan, matcher.start(), matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (matchPictureListener != null) {
                    matchPictureListener.match(bitmap, matcher.start(), matcher.end());
                }
            }
        }
        return spannable;
    }

    public static Spannable paragraph(String str, int index) {
        Spannable res;
        String[] array = str.split("\n", index + 2);
        Log.d(TAG, "paragraph: " + array.length);
        if (index < array.length) {
            res = contentToString(array[index]);
        } else {
            res = new SpannableString("");
        }
        return res;
    }

    @NonNull
    public static String rmStartWhiteChar(String str) {
        int i;
        for (i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                break;
            }
        }
        return str.substring(i);
    }

    public static void rmText(Context context, CharSequence charSequence) {
        Matcher matcher = Pattern.compile(PIC_TAG).matcher(charSequence);
        // 删除插入图片时，删除图片文件
        while (matcher.find()) {
            Matcher m = Pattern.compile(PIC_NAME).matcher(matcher.group());
            if (m.find()) {
                context.deleteFile(m.group());
//                Toast.makeText(context, "删除图片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
