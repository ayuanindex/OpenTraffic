package com.realmax.cars.tcputil;

import com.google.gson.Gson;
import com.realmax.cars.bean.BodyBean;
import com.realmax.cars.utils.EncodeAndDecode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars.tcputil
 * @ClassName: TCPConnected
 * @CreateDate: 2020/3/16 09:32
 */
public class TCPConnected {
    private static final String TAG = "TCPConnected";
    private static Socket socket = null;
    private static StringBuilder result = new StringBuilder("");
    private static boolean isRead = true;
    private static boolean flag = false;
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
     */
    public static void start(String host, int port, ResultData resultData) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socket = new Socket(host, port);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    if (socket.isConnected()) {
                        isRead = false;
                    }
                    resultData.isConnected(socket.isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
                    byte[] combine = option(EncodeAndDecode.getStrUnicode("{\"cmd\": \"start\", \"deviceType\": \"" + device_type + "\", \"deviceId\": " + device_id + ", \"cameraNum\": " + camera_num + "}"));
                    outputStream.write(combine);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    /**
     * 停止拍照
     */
    public static void stop_camera() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    isRead = false;
                    String command = "{\"cmd\": \"stop\"}";
                    outputStream.write(combine(new byte[]{(byte) 0xff, (byte) 0xaa, 0x02, 0x15, 0x00, 0x00, 0x00}, command.getBytes(), new byte[]{(byte) 0xeb, (byte) 0xff, 0x55}));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 将需要发送的消息加工成服务端可识别的数据
     *
     * @param command 需要发送的指令
     * @return 返回数据的byte数组
     */
    private static byte[] option(String command) {
        // 指令
        byte[] commandBytes = command.getBytes();
        int size = commandBytes.length + 10;
        int head_len = size - 4;
        // 帧长度=帧头+版本号+长度+帧尾
        byte[] lens = Int2Bytes_LE(head_len);
        // 加和校验=协议版本号+帧长度+数据
        byte[] combine = combine(new byte[]{0x02}, lens, commandBytes, new byte[]{0x00, (byte) 0xff55});
        int checkSum = checkSum(combine, size);
        //*option:��S   {"cmd": "start", "deviceType": "\u5c0f\u8f66", "deviceId": 1, "cameraNum": 1}��U*/
        /*??S   {"cmd": "start", "deviceType": "\u5c0f\u8f66", "deviceId": 1, "cameraNum": 1}??U*/
        return combine(
                new byte[]{
                        (byte) 0xff,
                        (byte) 0xaa,
                        0x02,
                        (byte) Integer.parseInt(Integer.toHexString(lens[0]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[1]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[2]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[3]), 16)
                },
                commandBytes,
                new byte[]{
                        (byte) Integer.parseInt(Integer.toHexString(checkSum), 16),
                        (byte) 0xff,
                        0x55
                }
        );
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

    /**
     * 任意个byte数组合并
     *
     * @param bytes
     * @return 发挥合并后的byte数组
     */
    public static byte[] combine(byte[]... bytes) {
        // 开始合并的位置
        int position = 0;
        // 新数组的总长度
        int length = 0;
        // 算出新数组的总长度
        for (byte[] aByte : bytes) {
            length += aByte.length;
        }
        // 创建一个新的byte数组
        byte[] ret = new byte[length];
        // 将byte数组合并成一个byte数组
        for (byte[] aByte : bytes) {
            System.arraycopy(aByte, 0, ret, position, aByte.length);
            position += aByte.length;
        }
        return ret;
    }

    public static BodyBean fetch_camera() {
        if (socket == null) {
            return null;
        }

        try {
            char left = '{';
            char right = '}';
            byte[] bytes = new byte[1024];
            int read = inputStream.read(bytes);
            String s = new String(bytes, 0, read);
            for (char c : s.toCharArray()) {
                if (c == left) {
                    flag = true;
                }
                if (flag) {
                    result.append(c);
                }
                if (c == right) {
                    flag = false;
                    String string = result.toString();
                    result = new StringBuilder("");
                    BodyBean bodyBean = new Gson().fromJson(string, BodyBean.class);
                    return bodyBean;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将返回的数据提取出来
     *
     * @param data 需要转换的数据
     * @return 返回json对象
     */
    public static String getResult(String data) {
        return data.substring(73, data.length() - 2);
    }

    public interface ResultData {
        void isConnected(boolean isConnected);

        void getResultData(String data);
    }
}
