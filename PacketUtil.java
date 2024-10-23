package com.jointech.sdk.jt709.utils;

import com.jointech.sdk.jt709.constants.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 解析包预处理（进行反转义）
 * @author HyoJung
 */
public class PacketUtil {
    /**
     * 解析消息包
     *
     * @param in
     * @return
     */
    public static Object decodePacket(ByteBuf in) {
        //可读长度不能小于基本长度
        if (in.readableBytes() < Constant.BINARY_MSG_BASE_LENGTH) {
            return null;
        }

        //防止非法码流攻击，数据太大为异常数据
        if (in.readableBytes() > Constant.BINARY_MSG_MAX_LENGTH) {
            in.skipBytes(in.readableBytes());
            return null;
        }

        //查找消息尾，如果未找到则继续等待下一包
        in.readByte();
        int tailIndex = in.bytesBefore(Constant.BINARY_MSG_HEADER);
        if (tailIndex < 0) {
            in.resetReaderIndex();
            return null;
        }

        int bodyLen = tailIndex;
        //创建ByteBuf存放反转义后的数据
        ByteBuf frame = ByteBufAllocator.DEFAULT.heapBuffer(bodyLen + 2);
        frame.writeByte(Constant.BINARY_MSG_HEADER);
        //消息头尾之间的数据进行反转义
        unescape(in, frame, bodyLen);
        in.readByte();
        frame.writeByte(Constant.BINARY_MSG_HEADER);

        //反转义后的长度不能小于基本长度
        if (frame.readableBytes() < Constant.BINARY_MSG_BASE_LENGTH) {
            ReferenceCountUtil.release(frame);
            return null;
        }
        return frame;
    }

    /**
     * 消息头、消息体、校验码中0x7D 0x02反转义为0x7E，0x7D 0x01反转义为0x7D
     *
     * @param in
     * @param frame
     * @param bodyLen
     */
    public static void unescape(ByteBuf in, ByteBuf frame, int bodyLen) {
        int i = 0;
        while (i < bodyLen) {
            int b = in.readUnsignedByte();
            if (b == 0x7D) {
                int nextByte = in.readUnsignedByte();
                if (nextByte == 0x01) {
                    frame.writeByte(0x7D);
                } else if (nextByte == 0x02) {
                    frame.writeByte(0x7E);
                } else {
                    //异常数据
                    frame.writeByte(b);
                    frame.writeByte(nextByte);
                }
                i += 2;
            } else {
                frame.writeByte(b);
                i++;
            }
        }
    }

    /**
     * 消息头、消息体、校验码中0x7E转义为0x7D 0x02，0x7D转义为0x7D 0x01
     *
     * @param out
     * @param bodyBuf
     */
    public static void escape(ByteBuf out, ByteBuf bodyBuf) {
        while (bodyBuf.readableBytes() > 0) {
            int b = bodyBuf.readUnsignedByte();
            if (b == 0x7E) {
                out.writeShort(0x7D02);
            } else if (b == 0x7D) {
                out.writeShort(0x7D01);
            } else {
                out.writeByte(b);
            }
        }
    }

    /**
     * 回复内容
     * @param terminalNumArr
     * @param msgFlowId
     * @return
     */
    public static String replyBinaryMessage(byte[] terminalNumArr,int msgFlowId) {
        //去除包头包尾的长度
        int contentLen = Constant.BINARY_MSG_BASE_LENGTH + 4;
        ByteBuf bodyBuf = ByteBufAllocator.DEFAULT.heapBuffer(contentLen-2);
        ByteBuf replyBuf = ByteBufAllocator.DEFAULT.heapBuffer(25);
        try {
            //消息ID
            bodyBuf.writeShort(0x8001);
            //数据长度
            bodyBuf.writeShort(0x0005);
            //终端ID
            bodyBuf.writeBytes(terminalNumArr);
            Random random = new Random();
            //生成1-65534内的随机数
            int index = random.nextInt() * (65534 - 1 + 1) + 1;
            //当前消息流水号
            bodyBuf.writeShort(index);
            //回复消息流水号
            bodyBuf.writeShort(msgFlowId);
            //应答的消息ID
            bodyBuf.writeShort(0x0200);
            //应答结果
            bodyBuf.writeByte(0x00);
            //校验码
            int checkCode = CommonUtil.xor(bodyBuf);
            bodyBuf.writeByte(checkCode);
            //包头
            replyBuf.writeByte(Constant.BINARY_MSG_HEADER);
            //读指针重置到起始位置
            bodyBuf.readerIndex(0);
            //转义
            PacketUtil.escape(replyBuf, bodyBuf);
            //包尾
            replyBuf.writeByte(Constant.BINARY_MSG_HEADER);
            return ByteBufUtil.hexDump(replyBuf);
        } catch (Exception e) {
            ReferenceCountUtil.release(replyBuf);
            return "";
        }
    }

    /**
     * 授时指令回复
     * @param itemList
     */
    public static String replyBASE2Message(List<String> itemList) {
        try {
            //设置日期格式
            ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
            String strBase2Reply = String.format("(%s,%s,%s,%s,%s,%s)", itemList.get(0), itemList.get(1)
                    , itemList.get(2), itemList.get(3), itemList.get(4), DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime));
            return strBase2Reply;
        }catch (Exception e) {
            return "";
        }
    }
}
