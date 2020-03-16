package com.realmax.cars.tcputil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final String TAG = "TCPConnected";
    private static Socket socket = null;
    /**
     * 输入流：读取数据
     */
    private static InputStream inputStream = null;
    /**
     * 输出流：发送数据
     */
    private static OutputStream outputStream = null;

    public static Socket getSocket() {
        return socket;
    }

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
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
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
                socket = null;
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
        if (socket == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("cmd", "start");
                    hashMap.put("deviceType", device_type);
                    hashMap.put("device_id", device_id);
                    hashMap.put("cameraNum", camera_num);
                    // 将传入参数转换成json字符串
                    String command = getJsonString(hashMap);

                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public static int checkSum(byte[] bytes, int size) {
        int cs = 0;
        int i = 2;
        int j = size - 3;
        while (i < j) {
            cs += bytes[i];
            i += 1;
        }
        return cs & 0xff;
    }

    /**
     * int转换为小端byte[]（高位放在高地址中）
     *
     * @param iValue
     * @return
     */
    public static byte[] Int2Bytes_LE(int iValue) {
        byte[] rst = new byte[4];
        // 先写int的最后一个字节
        rst[0] = (byte) (iValue & 0xFF);
        // int 倒数第二个字节
        rst[1] = (byte) ((iValue & 0xFF00) >> 8);
        // int 倒数第三个字节
        rst[2] = (byte) ((iValue & 0xFF0000) >> 16);
        // int 第一个字节
        rst[3] = (byte) ((iValue & 0xFF000000) >> 24);
        return rst;
    }

    public static String fetch_camera() {
        if (socket == null) {
            return "";
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            return getData(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
        return data.substring(7, data.length() - 3);
    }
}
