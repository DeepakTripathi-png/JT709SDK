package com.jointech.sdk.jt709.utils;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>Description: 用来存储一些解析中遇到的公共方法</p>
 *
 * @author lenny
 * @version 1.0.1
 */
public class CommonUtil {
    /**
     * 去掉字符串最后一个字符
     * @param inStr 输入的字符串
     * @param suffix 需要去掉的字符
     * @return
     */
    public static String trimEnd(String inStr, String suffix) {
        while(inStr.endsWith(suffix)){
            inStr = inStr.substring(0,inStr.length()-suffix.length());
        }
        return inStr;
    }

    /**
     * 16进制转byte[]
     * @param hex
     * @return
     */
    public static byte[] hexStr2Byte(String hex) {
        ByteBuffer bf = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            String hexStr = hex.charAt(i) + "";
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            bf.put(b);
        }
        return bf.array();
    }

    /**
     * 转换GPS时间
     *
     * @param bcdTimeStr
     * @return
     */
    public static ZonedDateTime parseBcdTime(String bcdTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(bcdTimeStr, formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime;
    }

    /**
     * 每个字节进行异或求值
     *
     * @param buf
     * @return
     */
    public static int xor(ByteBuf buf) {
        int checksum = 0;
        while (buf.readableBytes() > 0) {
            checksum ^= buf.readUnsignedByte();
        }
        return checksum;
    }
}
