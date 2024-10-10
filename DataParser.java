package com.jointech.sdk.jt709;

import com.alibaba.fastjson.JSONArray;
import com.jointech.sdk.jt709.constants.Constant;
import com.jointech.sdk.jt709.utils.CommonUtil;
import com.jointech.sdk.jt709.utils.ParserUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * <p>Description: 解析方法的主体</p>
 *
 * @author lenny
 * @version 1.0.1
 */
public class DataParser {
    /**
     * 解析Hex字符串原始数据
     * @param strData 16进制字符串
     * @return
     */
    public static Object receiveData(String strData) {
        int length=strData.length()%2>0?strData.length()/2+1:strData.length()/2;
        ByteBuf msgBodyBuf = Unpooled.buffer(length);
        msgBodyBuf.writeBytes(CommonUtil.hexStr2Byte(strData));
        return receiveData(msgBodyBuf);
    }

    /**
     * 解析byte[]原始数据
     * @param bytes
     * @return
     */
    private static Object receiveData(byte[] bytes)
    {
        ByteBuf msgBodyBuf =Unpooled.buffer(bytes.length);
        msgBodyBuf.writeBytes(bytes);
        return receiveData(msgBodyBuf);
    }

    /**
     * 解析ByteBuf原始数据
     * @param in
     * @return
     */
    private static Object receiveData(ByteBuf in)
    {
        Object decoded = null;
        in.markReaderIndex();
        int header = in.readByte();
        if (header == Constant.TEXT_MSG_HEADER) {
            in.resetReaderIndex();
            decoded = ParserUtil.decodeTextMessage(in);
        } else if (header == Constant.BINARY_MSG_HEADER) {
            in.resetReaderIndex();
            decoded = ParserUtil.decodeBinaryMessage(in);
        } else {
            return null;
        }
        return JSONArray.toJSON(decoded).toString();
    }
}
