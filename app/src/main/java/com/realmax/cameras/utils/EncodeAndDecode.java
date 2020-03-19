package com.realmax.cameras.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
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
