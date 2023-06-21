package cn.edu.whut.androidwebsocketclient.handler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.edu.whut.androidwebsocketclient.MainActivity;
import cn.edu.whut.androidwebsocketclient.MyApplication;

/**
 * 捕获异常
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    MyApplication application;

    public CrashHandler(MyApplication myApplication) {
        // // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = myApplication;
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 重启app
        Intent intent = new Intent(application.getApplicationContext(), MainActivity.class);
        //PendingIntent restartIntent = PendingIntent.getActivity(application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(MyApplication.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 退出程序
        AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
        // 3秒后重启
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 3000, restartIntent);
        //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
        Log.e(TAG, "Crash!!!");
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}

