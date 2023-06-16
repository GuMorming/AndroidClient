package cn.edu.whut.androidwebsocketclient.websocket;

import static cn.edu.whut.androidwebsocketclient.constants.DEVICE.DEVICE_NAME;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_AVAIL_MEMORY;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_CONNECT;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_GREETING;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_LEAVE;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_SCREENSHOT;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_SCREENSHOT_STOP;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_TOTAL_MEMORY;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.whut.androidwebsocketclient.entity.ClientMessage;
import cn.edu.whut.androidwebsocketclient.entity.MonitorMessage;
import cn.edu.whut.androidwebsocketclient.util.LogWrapper;

/**
 * @author by Talon, Date on 2020-04-13.
 * note: websocket 客户端
 */
public class MWebSocketClient extends WebSocketClient {

    private final String TAG = "MWebSocketClient";

    private boolean mIsConnected = false;
    public int screenshotNum = 0;
    private CallBack mCallBack;

    Timer timer;
    private ActivityManager.MemoryInfo mi;
    private ActivityManager activityManager;

    public MWebSocketClient(URI serverUri, CallBack callBack, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
        this.mCallBack = callBack;

    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.i(TAG, "onOpen");
        updateClientStatus(true, COMMAND_CONNECT);
        try {
            getSocket().setReceiveBufferSize(5 * 1024 * 1024); // 接收缓冲区大小
            activityManager.getMemoryInfo(mi);
            //总内存
            long totalMem = mi.totalMem / 1048576L;
            ClientMessage totalMemMessage = new ClientMessage(COMMAND_TOTAL_MEMORY, totalMem + "");
            send(totalMemMessage.toJson().toString());
            // 低内存阈值
//            Log.i(TAG, "低内存阈值:" + mi.threshold / 1048576L);
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    activityManager.getMemoryInfo(mi);
                    long availMem = mi.availMem / 1048576L;
                    //可用内存，字节为单位，转换为以MB为单位1048576=1024*1024
//                    Log.i(TAG, "可用内存:" + mi.availMem / 1048576L);
                    ClientMessage availMemMessage = new ClientMessage(COMMAND_AVAIL_MEMORY, availMem + "");
                    send(availMemMessage.toJson().toString());
                }
            };
            timer.schedule(task, 0, 1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息事件, 根据不同的"command"码来执行不同业务操作
     *
     * @param jsonMessage The UTF-8 decoded message that was received.
     */
    @Override
    public void onMessage(String jsonMessage) {
        Log.i("ClientReceived", jsonMessage);
        try {
            // 将web-monitor发来的消息转为json格式
            JSONObject jsonObject = new JSONObject(jsonMessage);
            MonitorMessage message = new MonitorMessage(jsonObject.toString());
            if (message.getUsername() != null && message.getCommand() != null) {
                switch (message.getCommand()) {
                    // 问候消息
                    case COMMAND_GREETING:
                        sendGreetingToServer();
                        break;
                    // 开始实时截图
                    case COMMAND_SCREENSHOT:
                        updateClientStatus(true, COMMAND_SCREENSHOT);
                        break;
                    // 停止截图
                    case COMMAND_SCREENSHOT_STOP:
                        updateClientStatus(true, COMMAND_SCREENSHOT_STOP);
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendGreetingToServer() throws JSONException {
        ClientMessage message = new ClientMessage(COMMAND_GREETING, "This is Client,Hi!");
        send(message.toJson().toString());
    }

    /**
     * @param bytes The binary message that was received.
     */
    @Override
    public void onMessage(ByteBuffer bytes) {
//        byte[] buf = new byte[bytes.remaining()];
//        bytes.get(buf);
//        if (mCallBack != null)
//            mCallBack.onBitmapReceived(BitmapUtils.decodeImg(buf));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, reason);
        timer.cancel();
        updateClientStatus(false, COMMAND_LEAVE);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "onError()" + ex.getMessage());
        timer.cancel();
        ex.printStackTrace();
        updateClientStatus(false, "error");
    }

    private void updateClientStatus(boolean isConnected, String command) {
        mIsConnected = isConnected;
        LogWrapper.d(TAG, "mIsConnected:" + mIsConnected);
        // 回调
        if (mCallBack != null)
            mCallBack.onClientStatus(isConnected, command);
    }

    public boolean isConnected() {
        LogWrapper.d(TAG, "mIsConnected:" + mIsConnected);
        return mIsConnected;
    }

    /**
     * 回调方法
     */
    public interface CallBack {
        void onClientStatus(boolean isConnected, String command);

    }

    public void setMi(ActivityManager.MemoryInfo mi) {
        this.mi = mi;
    }

    public void setActivityManager(ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

}
