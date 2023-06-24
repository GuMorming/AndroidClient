package cn.edu.whut.androidwebsocketclient.util;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class CPUInfoUtils {
    public static final String TAG = "CPUInfoUtils";

    public static float cpuUsage = 0;
    public static long totalMem;
    public static JSONObject processesJson;

    /**
     * 仅能获取本进程CPU使用率
     *
     * @return
     */
    public static float getCpuUsage() {
        cpuUsage = 0;
        java.lang.Process process = null;
        try {
            //调用shell 执行 "top -n 1"
            process = Runtime.getRuntime().exec("top -n 1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int cpuIndex = -1;
            // 逐行读取
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // 去除首尾空格
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                // 读取到"PID"说明下一行开始为各进程信息行
                if (line.contains("PID")) {
                    // 且本行为表头行,获取CPU信息的列号
                    cpuIndex = getCPUIndex(line);
                    // 开始读取信息
                    while ((line = reader.readLine()) != null) {
                        line = line.trim(); // 仍去除首尾空格
                        if (TextUtils.isEmpty(line)) {
                            continue;
                        }
                        // 以空格分割各数据
                        String[] cols = line.split("\\s+");
//                        Log.i(TAG, "Cols: " + Arrays.toString(cols));
                        // 获取cpu使用率
                        if (cols.length <= cpuIndex) {
                            continue;
                        }
                        String cpuCol = cols[cpuIndex];
                        // top命令按CPU使用率排序, 读取到"0.0"说明后续均为"0.0", 不再读取
                        if (cpuCol.equals("0.0")) {
                            break;
                        }
                        if (cpuCol.equals("R")) {
                            cpuCol = cols[cpuIndex + 1];
                        }
                        cpuUsage += Float.parseFloat(cpuCol);
                    }
                    // 退出循环, 不再读取
                    break;
                }
            }
            // 除去CPU核心数为实际使用率
            return cpuUsage / Runtime.getRuntime().availableProcessors();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getCPUIndex(String line) {
        if (line.contains("CPU")) {
            // 以空格分割
            String[] titles = line.split("\\s+");
            for (int i = 0; i < titles.length; i++) {
                if (titles[i].contains("CPU")) {
//                    Log.i(TAG, "CPU Index: " + i);
                    return i;
                }
            }
        }
        return -1;
    }

    public static void getProcessInfo() {
        java.lang.Process process = null;

        try {
            // 调用shell 执行 "top -s 10 -n 1"
            // 获取前十大占用内存进程
            process = Runtime.getRuntime().exec("top -s 10 -m 10 -n 1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            StringBuilder stringBuilder = new StringBuilder();
            String line;
            int packageIndex = -1;
            int memIndex = -1;
            // 逐行读取
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // 去除首尾空格
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
//                stringBuilder.append(line).append("\n");
                // 读取到"PID"说明下一行开始为各进程信息行
                if (line.contains("PID")) {
                    // 且本行为表头行,获取Mem信息和Package信息的列号
//                    packageIndex = getPackageIndex(line);
                    int cpuIndex = getCPUIndex(line);
                    memIndex = getMemIndex(line) - 1;
                    // 开始读取信息
                    while ((line = reader.readLine()) != null) {
                        line = line.trim(); // 仍去除首尾空格
                        if (TextUtils.isEmpty(line)) {
                            continue;
                        }
                        // 以空格分割各数据
                        String[] cols = line.split("\\s+");
//                        Log.i(TAG, "Cols: " + Arrays.toString(cols));
                        // 获取cpu使用率
                        if (cols.length <= memIndex) {
                            continue;
                        }
                        String memCol = cols[memIndex];
                        String packageCol = cols[memIndex + 2];
                        // 读到0.0说明不足10个进程占用内存, 退出循环
//                        if (memCol.equals("0.0")) {
//                            break;
//                        }

                    }
                    // 退出循环, 不再读取
                    break;
                }
            }
//            Log.i(TAG,stringBuilder.toString());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }


    private static int getMemIndex(String line) {
        if (line.contains("MEM")) {
            // 以空格分割
            String[] titles = line.split("\\s+");
            for (int i = 0; i < titles.length; i++) {
                if (titles[i].contains("MEM")) {
//                    Log.i(TAG, "MEM Index:" + i);
                    return i;
                }
            }
        }
        return -1;
    }

}
