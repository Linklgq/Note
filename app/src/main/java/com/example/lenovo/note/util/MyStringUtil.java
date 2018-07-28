package com.example.lenovo.note.util;

/**
 * Created by Lenovo on 2018/7/28.
 */

public class MyStringUtil {
    public static String getFirstChar(String str){
        char c=str.charAt(0);
        if(c>='a'&&c<='z'){
            c=(char)(c-'a'+'A');
        }
        return String.valueOf(c);
    }
}
