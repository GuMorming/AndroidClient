package cn.edu.whut.androidwebsocketclient;

import static cn.edu.whut.androidwebsocketclient.constants.CONFIG.POOL_NAME_CLIENT;
import static cn.edu.whut.androidwebsocketclient.constants.DEVICE.DEVICE_UID;
import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.*;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cn.edu.whut.androidwebsocketclient.broadcastReceiver.BatteryReceiver;
import cn.edu.whut.androidwebsocketclient.broadcastReceiver.NetworkReceiver;
import cn.edu.whut.androidwebsocketclient.constants.CONFIG;
import cn.edu.whut.androidwebsocketclient.entity.ClientMessage;
import cn.edu.whut.androidwebsocketclient.util.BitmapUtils;
import cn.edu.whut.androidwebsocketclient.util.CPUInfoUtils;
import cn.edu.whut.androidwebsocketclient.util.NetSpeedUtils;
import cn.edu.whut.androidwebsocketclient.util.ScreenShotHelper;
import cn.edu.whut.androidwebsocketclient.websocket.MWebSocketClient;
import cn.hutool.core.codec.Base64;


public class MainActivity extends AppCompatActivity implements ScreenShotHelper.OnScreenShotListener, MWebSocketClient.CallBack {
    private final String TAG = "MainActivity";
    private static final int REQUEST_MEDIA_PROJECTION = 100;

    private TextView connectStatusText;

    Timer timer = new Timer();
    TimerTask task;
    boolean isTimerOn = false;
    //android.app.ActivityManager.MemoryInfo
    private ActivityManager.MemoryInfo mi;
    //android.app.ActivityManager
    private ActivityManager activityManager;
    // 接收网络状态
    private NetworkReceiver networkReceiver;
    public static String NETWORK;
    // 接收电量改变状态
    private BatteryManager batteryManager;
    private BatteryReceiver batteryReceiver;
//    public static String batteryPercent;

    private ScreenShotHelper screenShotHelper;

    public static MWebSocketClient webSocketClient;
    private boolean socketIsStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button occupyCPUBtn = findViewById(R.id.occupyCPU_btn);
//        Button stopBtn = findViewById(R.id.btn_stop);
        connectStatusText = findViewById(R.id.connectStatus_text);

        try {
            URI url = new URI(CONFIG.URI_CONNECT);
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("username", DEVICE_UID);
            httpHeaders.put("poolName", POOL_NAME_CLIENT);

            //  真机调试用
//            webSocketClient = new MWebSocketClient(new URI(CONFIG.URI_CONNECT_DEBUG), this, httpHeaders);
            webSocketClient = new MWebSocketClient(url, this, httpHeaders);
            webSocketClient.setConnectionLostTimeout(5);
            boolean flag = webSocketClient.connectBlocking(1, TimeUnit.SECONDS); // 开始连接
            while (!flag) {
                Log.i(TAG, "Reconnecting...");
                //  真机调试用
//                webSocketClient = new MWebSocketClient(new URI(CONFIG.URI_CONNECT_DEBUG), this, httpHeaders);
                webSocketClient = new MWebSocketClient(url, this, httpHeaders);
                webSocketClient.setConnectionLostTimeout(5);
                flag = webSocketClient.connectBlocking(2, TimeUnit.SECONDS); // 开始连接
            }
            socketIsStarted = true;

            //系统内存情况
            activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            mi = new ActivityManager.MemoryInfo();


            //注册网络状态监听广播
            networkReceiver = new NetworkReceiver();
            IntentFilter networkFilter = new IntentFilter();
            networkFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            networkFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkReceiver, networkFilter);
            // 注册电量状态监听广播
//            batteryReceiver = new BatteryReceiver();
//            IntentFilter batteryFilter = new IntentFilter();
//            batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//            registerReceiver(batteryReceiver, batteryFilter);
            // 主动获取电量
            batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
//            int percent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);///当前电量百分比
//            Log.d(TAG, percent + "%");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 耗CPU语句
        Timer useCPUTimer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    int sum = 1;
                    for (int i = 1; i <= 5; i++) {
                        sum *= i;
                    }
                }
            }
        };
        occupyCPUBtn.setOnClickListener(v -> useCPUTimer.schedule(task1, 0));
//        stopBtn.setOnClickListener(v -> {
//            timer.cancel();
//            timer = new Timer();
//        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销之前已注册的广播
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

    @Override
    public void onClientStatus(boolean isConnected, String command) {
        switch (command) {
            case COMMAND_SELECT:
                sendDeviceInfoToServer();
                break;
            case COMMAND_SCREENSHOT:
                webSocketClient.screenshotNum++;
                tryStartScreenShot();
                break;
            case COMMAND_SCREENSHOT_CANCEL:
                webSocketClient.screenshotNum--;
                screenshotCancel();
                break;
            case COMMAND_SCREENSHOT_STOP:
                webSocketClient.screenshotNum--;
                if (webSocketClient.screenshotNum <= 0) {
                    screenShotHelper.stopScreenShot();
                }
                break;
            case COMMAND_CONNECT:
                connectStatusText.setText("已连接!");
                break;
            case "error":
            case COMMAND_LEAVE:
                timer.cancel();
                isTimerOn = false;
                connectStatusText.setText("未连接");
                Log.e(TAG, "COMMAND_LEAVE");
                break;
            default:
                break;
        }
    }

    public void sendDeviceInfoToServer() {
        activityManager.getMemoryInfo(mi);
        //总内存(字节为单位), 转换为MB 1024*1024 = 1048576
        long totalMem = mi.totalMem / 1048576L;
        CPUInfoUtils.totalMem = totalMem;
        ClientMessage totalMemMessage = new ClientMessage(COMMAND_TOTAL_MEMORY, totalMem + "");
        webSocketClient.send(totalMemMessage.toJson().toString());
        task = new TimerTask() {
            @Override
            public void run() {
                activityManager.getMemoryInfo(mi);
                //可用内存(字节为单位), 转换为MB 1024*1024 = 1048576
                long availMemory = mi.availMem / 1048576L;
                try {
                    JSONObject jsonObject = new JSONObject();
                    String CPU_USAGE = String.valueOf(CPUInfoUtils.getCpuUsage());
                    String NET_SPEED = NetSpeedUtils.getNetSpeed();
                    String batteryLevel = String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
                    jsonObject.put(KEY_COMMAND, COMMAND_DEVICE_INFO);
                    // 可用内存
                    jsonObject.put(KEY_AVAILABLE_MEMORY, availMemory);
                    // CPU使用率
                    jsonObject.put(KEY_CPU_USAGE, CPU_USAGE);
                    // 网络状态
                    jsonObject.put(KEY_NETWORK, NETWORK);
                    // 网速
                    jsonObject.put(KEY_NET_SPEED, NET_SPEED);
                    // 电量
                    jsonObject.put(KEY_BATTERY_LEVEL, batteryLevel);
                    webSocketClient.send(jsonObject.toString());
                    Log.i(TAG, String.valueOf(CONFIG.IMAGE_QUALITY));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        if (isTimerOn) {
            timer.cancel();
            timer = new Timer();
        }
        // 每1秒执行一次
        timer.schedule(task, 0, 1000);
        isTimerOn = true;
    }

    /**
     * Client端：1. 开启服务  2. 申请截图权限  3. 传输数据
     *
     * @param view
     */
    public void StartQuick(View view) {
        if (!socketIsStarted) {
            Toast.makeText(this, "websocket 服务启动异常！", Toast.LENGTH_SHORT).show();
        } else {
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

    /**
     * 用户给予权限,则开始录屏;否则反馈给Monitor
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && data != null) {
            // 同意录屏
            if (resultCode == RESULT_OK) {
                // 截屏的回调
                screenShotHelper = new ScreenShotHelper(this, resultCode, data, this);
                screenShotHelper.startScreenShot();
            }
        }
        // 拒绝录屏
        else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "用户取消");
            onClientStatus(true, COMMAND_SCREENSHOT_CANCEL);
        }
    }

    /**
     * 用户取消截图
     */
    public void screenshotCancel() {
        ClientMessage clientMessage = new ClientMessage(COMMAND_SCREENSHOT_CANCEL, "");
        webSocketClient.send(clientMessage.toJson().toString());
    }

    /**
     * 截图完成后
     *
     * @param bitmap
     */
    @Override
    public void onShotFinish(Bitmap bitmap) {
        Log.d(TAG, "bitmap:" + bitmap.getAllocationByteCount() / 1024 + "kB");
        // 获取截图的字节流
        final byte[] byteBitmap = BitmapUtils.getByteBitmap(bitmap);
        // 用Base64编码
        String encodedBitmapStr = Base64.encode(byteBitmap);
        // 发送
        ClientMessage message = new ClientMessage(COMMAND_SCREENSHOT, encodedBitmapStr);
        webSocketClient.send(message.toJson().toString());
    }


}
