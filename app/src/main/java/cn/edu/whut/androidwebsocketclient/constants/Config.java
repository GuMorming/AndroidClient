package cn.edu.whut.androidwebsocketclient.constants;

import static cn.edu.whut.androidwebsocketclient.constants.DEVICE.DEVICE_NAME;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_CONNECT;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.KEY_COMMAND;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.KEY_POOL_NAME;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.KEY_USERNAME;

public class Config {

    public static final float IMAGE_SCALE = 0.4f;  // 设置缩放比例0.4 比较适合 (在ScreenShotHelper中设置)

    public static final int WINDOWS_SERVER_PORT = 8080;
    public static final String WINDOWS_SERVER_HOST = "10.0.2.2";
    public static final String END_POINT = "app-websocket";

    public static final String POOL_NAME_CLIENT = "client";

    // %7B='{', %7D='}', %22='"'
    public static final String INFO_CONNECT =
            "INFO="
                    + "%7B"
                    + "%22"+ KEY_COMMAND +"%22:" + "%22" + COMMAND_CONNECT + "%22"
                    + ",%22"+ KEY_USERNAME +"%22:" + "%22" + DEVICE_NAME + "%22"
                    + ",%22"+ KEY_POOL_NAME +"%22:" + "%22" + POOL_NAME_CLIENT + "%22"
                    + "%7D";
    public static final String URI_CONNECT =
            "ws://" + WINDOWS_SERVER_HOST+":"+WINDOWS_SERVER_PORT
                    +"/"+ END_POINT
                    +"/" + INFO_CONNECT;


}
