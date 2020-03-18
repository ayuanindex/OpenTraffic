package com.realmax.cars.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;


/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars.tcputil
 * @ClassName: EncodeAndDecode
 * @CreateDate: 2020/3/15 17:05
 */
public class EncodeAndDecode {

    /**
     * @param imgData 需要转换的base64编码
     * @return 返回Bitmap类型的图拍你
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap decodeBase64ToImage(String imgData) {  //对字节数组字符串进行Base64解码并生成图片
        //图像数据为空
        if (imgData == null) {
            return null;
        }

        try {
            // 获取Base64解码对象
            Decoder decoder = Base64.getDecoder();
            //Base64解码
            byte[] b = decoder.decode(imgData);
            for (int i = 0; i < b.length; ++i) {
                // 调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //使用BitmapFactory工厂类将流转换成图片
            return BitmapFactory.decodeStream(new ByteArrayInputStream(b));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 字符串转化成为16进制字符串
     *
     * @param s 需要转换的字符串
     * @return 返回16进制的字符串
     */
    public static String encodeStrTo16(String s) {
        StringBuilder str = new StringBuilder();
        // 循环将去除每个字符将其抓换成16进制
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str.append(s4);
        }
        return str.toString();
    }

    /**
     * 16进制转换成为string类型字符串
     *
     * @param s 需要转换的16进制字符串
     * @return 返回正常UTF8的字符串
     */
    public static String decode16ToStr(String s) {
        try {
            // 对传进来的字符串是否为空进行判断
            if (TextUtils.isEmpty(s)) {
                return "";
            }
            // 替换掉所有的空格
            s = s.replace(" ", "");
            byte[] baKeyword = new byte[s.length() / 2];
            for (int i = 0; i < baKeyword.length; i++) {
                try {
                    baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            s = new String(baKeyword, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 将字符串转换成Unicode编码
     *
     * @param inStr 需要抓换的字符串
     * @return 返回Unicode编码的字符串（为编码和编码的长度可能不同）
     */
    public static String getStrUnicode(String inStr) {
        StringBuffer unicode = new StringBuffer();
        char c;
        int bit;
        String tmp = null;
        for (int i = 0; i < inStr.length(); i++) {
            c = inStr.charAt(i);
            if (c > 255) {
                unicode.append("\\u");
                bit = (c >>> 8);
                tmp = Integer.toHexString(bit);
                if (tmp.length() == 1) unicode.append("0");
                unicode.append(tmp);
                bit = (c & 0xFF);
                tmp = Integer.toHexString(bit);
                if (tmp.length() == 1) unicode.append("0");
                unicode.append(tmp);
            } else {
                unicode.append(c);
            }
        }
        return (new String(unicode));
    }
}
