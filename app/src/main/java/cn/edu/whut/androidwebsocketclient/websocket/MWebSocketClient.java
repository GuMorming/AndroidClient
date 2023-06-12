package cn.edu.whut.androidwebsocketclient.websocket;

import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_GREETING;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.COMMAND_SCREENSHOT;

import android.graphics.Bitmap;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.URI;
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

    public MWebSocketClient(URI serverUri, CallBack callBack) {
        super(serverUri);
        this.mCallBack = callBack;
    }
    // 添加HEADER
    public MWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        LogWrapper.e(TAG, "onOpen");
        updateClientStatus(true);

        try {
            getSocket().setReceiveBufferSize(5 * 1024 * 1024);
        } catch (SocketException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMessage(String jsonMessage) {
        Log.i("ClientReceived", jsonMessage);
        try {
            // 将web-monitor发来的消息转为json格式
            JSONObject jsonObject = new JSONObject(jsonMessage);
            MonitorMessage message = new MonitorMessage(jsonObject.toString());
            if (message.getUsername() != null && message.getCommand() != null) {
                switch (message.getCommand()) {
                    case COMMAND_GREETING:
                        sendGreetingToServer();
                        break;
                    case COMMAND_SCREENSHOT:
                        sendBitmapToServer();
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
    public boolean sendBitmapToServer(){

        return true;
    }

//    @Override
//    public void onMessage(ByteBuffer bytes) {
//        byte[] buf = new byte[bytes.remaining()];
//        bytes.get(buf);
//        if (mCallBack != null)
//            mCallBack.onBitmapReceived(BitmapUtils.decodeImg(buf));
//    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

        updateClientStatus(false);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG,"onError()");
        updateClientStatus(false);
    }

    private void updateClientStatus(boolean isConnected) {

        mIsConnected = isConnected;
        LogWrapper.d(TAG, "mIsConnected:" + mIsConnected);
        // 回调
        if (mCallBack != null)
            mCallBack.onClientStatus(isConnected);
    }

    public boolean isConnected() {
        LogWrapper.d(TAG, "mIsConnected:" + mIsConnected);
        return mIsConnected;
    }

    public interface CallBack {
        void onClientStatus(boolean isConnected);

        void onBitmapReceived(Bitmap bitmap);
    }


}
