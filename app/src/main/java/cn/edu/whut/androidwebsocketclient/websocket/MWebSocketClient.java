package cn.edu.whut.androidwebsocketclient.websocket;

import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_GREETING;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_SCREENSHOT;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_SCREENSHOT_STOP;

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
    private CallBack mCallBack;

    public MWebSocketClient(URI serverUri, CallBack callBack,Map<String,String> httpHeaders) {
        super(serverUri,httpHeaders);
        this.mCallBack = callBack;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        LogWrapper.e(TAG, "onOpen");
        updateClientStatus(true, "connect");
        try {
            getSocket().setReceiveBufferSize(5 * 1024 * 1024); // 接收缓冲区大小
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息事件, 根据不同的"command"码来执行不同业务操作
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
                        sendBitmapToServer();
                        break;
                    // 停止截图
                    case COMMAND_SCREENSHOT_STOP:
                        stopSendBitMapToServer();
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
        ClientMessage message = new ClientMessage(COMMAND_GREETING,"This is Client,Hi!");
        send(message.toJson().toString());
    }
    public void sendBitmapToServer(){
        updateClientStatus(true,COMMAND_SCREENSHOT);
    }
    public void stopSendBitMapToServer(){
        updateClientStatus(true,COMMAND_SCREENSHOT_STOP);
    }

    /**
     *
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
        Log.i(TAG,reason);
        updateClientStatus(false, "close");
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG,"onError()"+ex.getMessage());
        ex.printStackTrace();
        updateClientStatus(false, "error");
    }

    private void updateClientStatus(boolean isConnected,String command) {
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


}
