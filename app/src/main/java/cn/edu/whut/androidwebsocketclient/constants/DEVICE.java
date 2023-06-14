package cn.edu.whut.androidwebsocketclient.constants;

import android.os.Build;
import android.provider.Settings;

import cn.edu.whut.androidwebsocketclient.util.IPUtils;

public class DEVICE {

//    public static final String DEVICE_NAME = Build.DEVICE;
    public static final String DEVICE_NAME = Build.DEVICE;
    public static final String DEVICE_IP = String.format("%s", IPUtils.getIpAddressString());
}
