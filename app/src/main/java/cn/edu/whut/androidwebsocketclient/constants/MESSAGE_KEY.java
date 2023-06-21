package cn.edu.whut.androidwebsocketclient.constants;

public class MESSAGE_KEY {
    // 连接
    public static final String COMMAND_CONNECT = "connect";
    public static final String COMMAND_SELECT = "select";
    // 录屏相关
    public static final String COMMAND_SCREENSHOT = "screenshot";
    public static final String COMMAND_SCREENSHOT_STOP = "screenshot_stop";
    public static final String COMMAND_SCREENSHOT_CANCEL = "screenshot_cancel";
    public static final String COMMAND_LOCK_SCREEN = "lockScreen";

    // 总内存
    public static final String COMMAND_TOTAL_MEMORY = "totalMemory";
    // 设备信息: 剩余内存, CPU使用率, 网速, 电量
    public static final String COMMAND_DEVICE_INFO = "deviceInfo";
    // 网络状态相关
    public static final String COMMAND_NETWORK = "network";

    // 连接与断开
    public static final String COMMAND_GREETING = "greeting";
    public static final String COMMAND_LEAVE = "leave";

    public static final String KEY_COMMAND = "command";
    public static final String KEY_POOL_NAME = "poolName";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_DATA = "data";
    public static final String KEY_AVAILABLE_MEMORY = "availMemory";
    public static final String KEY_CPU_USAGE = "cpuUsage";
    public static final String KEY_NETWORK = "network";
    public static final String KEY_NET_SPEED = "netSpeed";
    public static final String KEY_BATTERY_LEVEL = "batteryLevel";


}
