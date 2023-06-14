package cn.edu.whut.androidwebsocketclient;

import static cn.edu.whut.androidwebsocketclient.constants.Config.*;
import static cn.edu.whut.androidwebsocketclient.constants.DEVICE.DEVICE_NAME;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private TextView ip_textView;

    ScreenShotHelper screenShotHelper;

    private MWebSocketClient webSocketClient;
    private boolean socketIsStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip_textView = findViewById(R.id.ip_textView);

        URI url;
        try {
            url = new URI(Config.URI_CONNECT);
            Map<String,String> httpHeaders = new HashMap<>();
            httpHeaders.put("username", DEVICE_NAME);
            httpHeaders.put("poolName",POOL_NAME_CLIENT);
            webSocketClient = new MWebSocketClient(url, this, httpHeaders);
            webSocketClient.setConnectionLostTimeout(1);
            boolean flag = webSocketClient.connectBlocking(1, TimeUnit.SECONDS); // 开始连接
            while(!flag){
                Log.i(TAG,"Reconnecting...");
                webSocketClient = new MWebSocketClient(url, this, httpHeaders);
                webSocketClient.setConnectionLostTimeout(2);
                flag = webSocketClient.connectBlocking(2, TimeUnit.SECONDS); // 开始连接
            }
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
        ip_textView.setText(DEVICE.DEVICE_IP);
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
                screenShotHelper = new ScreenShotHelper(this, resultCode, data, this);
                screenShotHelper.startScreenShot();
            } else if (resultCode == RESULT_CANCELED) {
                LogWrapper.d(TAG, "用户取消");
            }
        }
    }

    /**
     * 截图完成后
     * @param bitmap
     * @throws InterruptedException
     */
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
    }




    @Override
    public void onClientStatus(boolean isConnected, String command) {
        switch (command){

            case COMMAND_SCREENSHOT:
                tryStartScreenShot();
                break;
            case COMMAND_SCREENSHOT_STOP:
                screenShotHelper.stopScreenShot();
                break;

            default:
                break;
        }
    }


}
