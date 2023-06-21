package cn.edu.whut.androidwebsocketclient;

import android.app.Application;
import android.content.Context;

import cn.edu.whut.androidwebsocketclient.handler.CrashHandler;

public class MyApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化-异常捕获(设置该CrashHandler为程序的默认处理器)
        CrashHandler unCeHandler = new CrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(unCeHandler);

    }

}
