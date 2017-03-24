package com.bill.download;

import android.app.Application;

/**
 * 全局通用application
 */

public class MyApplication extends Application {

    //全局的Context对象
    public static MyApplication sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;
    }

    public static MyApplication getInstance() {
        return sContext;
    }
}
