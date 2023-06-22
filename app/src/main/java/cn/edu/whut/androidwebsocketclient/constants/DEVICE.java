package cn.edu.whut.androidwebsocketclient.constants;

import android.os.Build;
import android.os.Process;

public class DEVICE {

    public static final String DEVICE_NAME = Build.DEVICE;
    public static final String DEVICE_UID = String.valueOf(Process.myUid());

}
