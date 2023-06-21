package cn.edu.whut.androidwebsocketclient.constants;

import android.os.Process;

public class DEVICE {

    //    public static final String DEVICE_NAME = Build.DEVICE;
    public static final String DEVICE_NAME = String.valueOf(Process.myUid());

}
