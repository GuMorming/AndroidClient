package cn.edu.whut.androidwebsocketclient.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 监听电量改变状态广播来获取电量(被动获取)
 */
public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int current = intent.getExtras().getInt("level");// 获得当前电量
        int total = intent.getExtras().getInt("scale");// 获得总电量
        int percent = current * 100 / total; // 电量百分比

//        batteryPercent = String.valueOf(percent);
//        Log.d(TAG, batteryPercent + "%");
    }

}
