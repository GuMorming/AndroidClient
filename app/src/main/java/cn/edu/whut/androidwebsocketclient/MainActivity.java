package cn.edu.whut.androidwebsocketclient;

import static cn.edu.whut.androidwebsocketclient.constants.Config.*;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import cn.edu.whut.androidwebsocketclient.constants.Config;
import cn.edu.whut.androidwebsocketclient.constants.DEVICE;
import cn.edu.whut.androidwebsocketclient.entity.ClientMessage;
import cn.edu.whut.androidwebsocketclient.util.BitmapUtils;
import cn.edu.whut.androidwebsocketclient.util.LogWrapper;
import cn.edu.whut.androidwebsocketclient.util.ScreenShotHelper;
import cn.edu.whut.androidwebsocketclient.websocket.MWebSocketClient;
import cn.hutool.core.codec.Base64;

/**
 * @author by talon, Date on 2020/6/20.
 * note: 主界面
 */
public class MainActivity extends AppCompatActivity implements ScreenShotHelper.OnScreenShotListener,MWebSocketClient.CallBack {
    private final String TAG = "MainActivity";
    private static final int REQUEST_MEDIA_PROJECTION = 100;
    private TextView tv_ip;

    private MWebSocketClient webSocketClient;
    private boolean socketIsStarted = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_ip = findViewById(R.id.tv_ip);

        URI url = null;
        try {
            url = new URI(Config.URI_CONNECT);
            Map<String,String> httpHeaders = new HashMap<>();
            httpHeaders.put("username",DEVICE.DEVICE_NAME);
            httpHeaders.put("poolName",POOL_NAME_CLIENT);
            webSocketClient = new MWebSocketClient(url, httpHeaders);
            webSocketClient.setConnectionLostTimeout(5 * 1000);
            boolean flag = webSocketClient.connectBlocking();
            Toast.makeText(this, "链接状态：" + flag, Toast.LENGTH_LONG).show();
            if(flag){
                socketIsStarted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_ip.setText(DEVICE.DEVICE_IP);
    }

    /**
     * 推送端：1. 开启服务  2. 申请截图权限  3. 传输数据
     *
     * @param view
     */
    public void StartQuick(View view) {
        if (!socketIsStarted) {
            Toast.makeText(this, "websocket 服务启动异常！", Toast.LENGTH_SHORT).show();
        }else {
            tryStartScreenShot();
        }
    }

    /**
     * 申请截屏权限
     */
    private void tryStartScreenShot() {
        MediaProjectionManager mProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && data != null) {
            if (resultCode == RESULT_OK) {
                // 截屏的回调
                ScreenShotHelper screenShotHelper = new ScreenShotHelper(this, resultCode, data, this);
                screenShotHelper.startScreenShot();
            } else if (resultCode == RESULT_CANCELED) {
                LogWrapper.d(TAG, "用户取消");
            }
        }
    }

    @Override
    public void onShotFinish(Bitmap bitmap) throws InterruptedException {
        LogWrapper.d(TAG, "bitmap:" + bitmap.getWidth());
        final byte[] byteBitmap = BitmapUtils.getByteBitmap(bitmap);
        String encodedBitmapStr = Base64.encode(byteBitmap);
        ClientMessage message = new ClientMessage(COMMAND_SCREENSHOT,encodedBitmapStr);
        if(webSocketClient.isOpen()){
            webSocketClient.send(message.toJson().toString());
        }else{
            webSocketClient.reconnectBlocking();
        }
//        String data = new String(byteBitmap, StandardCharsets.UTF_8);
//        ClientTextMessage message = new ClientTextMessage(COMMAND_SCREENSHOT,data);
//        mainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (webSocketClient.isOpen()) {
//                    webSocketClient.send(byteBitmap);
//                }else{
//                    try {
//                        webSocketClient.reconnectBlocking();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        });

    }


    @Override
    public void onClientStatus(boolean isConnected) {

    }

    @Override
    public void onBitmapReceived(Bitmap bitmap) {

    }
}
