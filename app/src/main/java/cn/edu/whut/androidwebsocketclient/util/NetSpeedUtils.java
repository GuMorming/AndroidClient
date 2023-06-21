package cn.edu.whut.androidwebsocketclient.util;

import android.net.TrafficStats;

/**
 * 网速
 */
public class NetSpeedUtils {

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;
    

    private static long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(android.os.Process.myUid()) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    public static String getNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        long speed2 = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 % (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;

        return speed + "." + speed2;
    }

}
