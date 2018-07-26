package com.example.lenovo.note;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by Lenovo on 2018/7/26.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext(){
        return context;
    }
}
