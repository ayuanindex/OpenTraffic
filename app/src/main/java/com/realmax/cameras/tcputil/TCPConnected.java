package com.realmax.cameras.tcputil;

import com.google.gson.Gson;
import com.realmax.cameras.bean.BodyBean;
import com.realmax.cameras.utils.EncodeAndDecode;

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
    private static StringBuilder result = new StringBuilder("");
    private static Socket socket = null;
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
     * @param host       地址
     * @param port       端口号
     * @param resultData 回调接口，返回是否已经连接成功的状态
     */
    public static void start(String host, int port, ResultData resultData) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    // 建立TCP连接
                    socket = new Socket(host, port);
                    // 获取输入里：获取数据
                    inputStream = socket.getInputStream();
                    // 获取输出流：发送数据
                    outputStream = socket.getOutputStream();
                    // 通过接口将连接状态返回出去
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
        // 对socket进行判空处理　
        if (socket == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    // 停止上一次的拍照
                    stop_camera();
                    // 准备好的拍照指令
                    String command = "{\"cmd\": \"start\", \"deviceType\": \"" + device_type + "\", \"deviceId\": " + device_id + ", \"cameraNum\": " + camera_num + "}";
                    // 通过EncodeAndDecode工具累中的getStrUnicode方法将需要传输的数据进行Unicode编码
                    // 通过option()对需要发送的指令进行数据加工（帧头，协议版本号，帧长度，checkSum验证，帧尾）
                    byte[] combine = option(EncodeAndDecode.getStrUnicode(command));
                    // 将加工好的数据发送至服务端
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
                    // 准备好的停止拍照指令
                    String command = "{\"cmd\": \"stop\"}";
                    // 数据加工
                    byte[] combine = option(command);
                    // 发送数据
                    outputStream.write(combine);
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
     * @return 返回即将要发送的数据的byte数组
     */
    private static byte[] option(String command) {
        // 将指令转换成byte数组（此处的指令是已经转换成了Unicode编码，如果不转换长度计算会有问题）
        byte[] commandBytes = command.getBytes();
        // 这里的长度是字节长度（总长度是数据的字节长度+其他数据的长度：帧头、帧尾……）
        int size = commandBytes.length + 10;
        // 帧长度=总长度-帧头的长度（2byte）-帧尾的长度(2byte)
        int head_len = size - 4;
        // 将帧长度转换成小端模式
        byte[] lens = Int2Bytes_LE(head_len);
        // 将需要验证的数据合并成一个byte数组
        // 将所有的参数放进去（其中帧头、协议版本号、帧尾是不变的数据）
        // 注意：需要将每个16进制的数据单独当成byte数组的一个元素，例：0xffaa -->  new byte[]{(byte) 0xff, (byte) 0xaa},需要拆分开
        byte[] combine = combine(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x02}, lens, commandBytes, new byte[]{(byte) 0x00, (byte) 0xff, (byte) 0x55});
        // 进行加和校验
        int checkSum = checkSum(combine, size);
        return combine(
                new byte[]{
                        (byte) 0xff,
                        (byte) 0xaa,
                        (byte) 0x02,
                        (byte) Integer.parseInt(Integer.toHexString(lens[0]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[1]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[2]), 16),
                        (byte) Integer.parseInt(Integer.toHexString(lens[3]), 16)
                },
                commandBytes,
                new byte[]{
                        (byte) Integer.parseInt(Integer.toHexString(checkSum), 16),
                        (byte) 0xff,
                        (byte) 0x55
                }
        );
    }

    /**
     * 加和校验
     *
     * @param bytes 需要校验的byte数组
     * @return 返回校验结果（16进制数据）
     */
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
     * @param iValue 需要转换的数字
     * @return 返回小端模式的byte数组
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
            // 参数1：待合并的数组
            // 参数2：开始合并的位置（从参数一的第n哥元素开始合并）
            // 参数3：合并的目标数组
            // 参数4：在目标数组的开始位置
            // 参数5：<=参数一的长度（这里取值为参数一的总长度相当于参数一的所有元素）
            System.arraycopy(aByte, 0, ret, position, aByte.length);
            // 计算合并下一个数组在新数组中的开始位置
            position += aByte.length;
        }
        return ret;
    }

    /**
     * 获取服务端返回的数据
     *
     * @return 返回照片的base64编码
     */
    public static String fetch_camera() {
        if (socket != null) {
            try {
                // 因为照片的base64编码格式的数据较多，服务端会一段一段的发送数据片段，不能够一下拿到整条数据
                // 数据中包含一段json格式的数据，所以可以用{}来作为整条数据的判定
                // 开始拼接数据的标记
                char left = '{';
                // 结束拼接数据的标记
                char right = '}';
                // 读取一段数据
                byte[] bytes = new byte[1024];
                int read = inputStream.read(bytes);
                String s = new String(bytes, 0, read);
                // 将数据进行遍历
                for (char c : s.toCharArray()) {
                    // 判断是否已经开始记录阶段
                    if (c == left) {
                        // 设置flag标记，将开始记录数据
                        flag = true;
                    }
                    if (flag) {
                        // 通过stringBuilder来拼接字符串
                        result.append(c);
                    }
                    // 判断是否已经是右边的括号
                    if (c == right) {
                        // flag设置为false停止记录
                        flag = false;
                        // 将StringBuilder记录的整段的字符串提取出来
                        String string = result.toString();
                        // 初始化StringBuilder
                        result = new StringBuilder("");
                        // 这里可以讲提取出来的json数据通过Gson第三方的包将json数据直接转换成JavaBean，
                        BodyBean bodyBean = new Gson().fromJson(string, BodyBean.class);
                        // 这里就直接截取了字符串，直接获取图片的信息
                        return getResult(string);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 将返回的数据提取出来
     *
     * @param data 需要截取
     * @return 返回json对象
     */
    public static String getResult(String data) {
        // 前72为都是其他信息的数据，后面少截2为是一个双引号加上一个大括号
        return data.substring(72, data.length() - 2);
    }

    // 回调接口，后期可拓展
    public interface ResultData {
        void isConnected(boolean isConnected);
    }
}
