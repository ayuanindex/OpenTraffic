package com.realmax.cars.tcputil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars.tcputil
 * @ClassName: TCPConnected
 * @CreateDate: 2020/3/16 09:32
 */
public class TCPConnected {
    private static Socket socket = null;
    /**
     * 输入流：读取数据
     */
    private static InputStream inputStream = null;
    /**
     * 输出流：发送数据
     */
    private static OutputStream outputStream = null;

    /**
     * 开启TCP连接
     *
     * @param host 地址
     * @param port 端口号
     * @return 返回连接的socket对象
     */
    public static Socket start(String host, int port) {
        try {
            socket = new Socket(host, port);
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭TCP连接
     */
    public static void stop() {
        try {
            // 关闭连接
            if (!socket.isClosed()) {
                socket.close();
            }

            // 关闭输出流
            if (inputStream != null) {
                inputStream.close();
            }

            // 关闭输入流
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送拍照指令
     *
     * @param device_type 设备类型
     * @param device_id   设备ID
     * @param camera_num  摄像头编号
     */
    public static void start_camera(String device_type, int device_id, int camera_num) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cmd", "start");
        hashMap.put("deviceType", device_type);
        hashMap.put("device_id", device_id);
        hashMap.put("cameraNum", camera_num);
        // 获取到json格式的指令
        String command = getJsonString(hashMap);

    }

    public static void stop_camera() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cmd", "stop");
        // 获取到json格式的指令
        String command = getJsonString(hashMap);
    }

    /**
     * 将Map集合转换成json字符串
     *
     * @param hashMap map集合
     * @return 返回json字符串
     */
    private static String getJsonString(HashMap<String, Object> hashMap) {
        JSONObject jsonObject = new JSONObject(hashMap);
        return jsonObject.toString();
    }

    /**
     * 将返回的数据提取出来
     *
     * @param data 需要转换的数据
     * @return 返回json对象
     */
    public static String getData(String data) {
        boolean flag = false;
        int count = 0;
        char leftChar = '{';
        char rightChar = '}';
        StringBuilder objcpy = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == leftChar) {
                flag = true;
                count++;
            }


            if (flag) {
                objcpy.append(c);
            }

            if (c == rightChar) {
                count--;
                if (count == 0) {
                    flag = false;
                    return new String(objcpy);
                }
            }
        }
        return null;
    }
}
