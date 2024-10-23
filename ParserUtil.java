package com.jointech.sdk.jt709.utils;

import com.jointech.sdk.jt709.constants.Constant;
import com.jointech.sdk.jt709.model.AlarmTypeEnum;
import com.jointech.sdk.jt709.model.LocationData;
import com.jointech.sdk.jt709.model.LockEvent;
import com.jointech.sdk.jt709.model.Result;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: 解析方法工具类</p>
 * @author HyoJung
 */
public class ParserUtil {
    /**
     * 定位数据解析0x0200
     * @param in
     * @return
     */
    public static Result decodeBinaryMessage(ByteBuf in) {
        //对数据进行反转移处理
        ByteBuf msg = (ByteBuf) PacketUtil.decodePacket(in);
        //消息长度
        int msgLen = msg.readableBytes();
        //包头
        msg.readByte();
        //消息ID
        int msgId = msg.readUnsignedShort();
        //消息体属性
        int msgBodyAttr = msg.readUnsignedShort();
        //消息体长度
        int msgBodyLen = msgBodyAttr & 0b00000011_11111111;
        //是否分包
        boolean multiPacket = (msgBodyAttr & 0b00100000_00000000) > 0;
        //去除消息体的基础长度
        int baseLen = Constant.BINARY_MSG_BASE_LENGTH;

        //根据消息体长度和是否分包得出后面的包长
        int ensureLen = multiPacket ? baseLen + msgBodyLen + 4 : baseLen + msgBodyLen;
        if (msgLen != ensureLen) {
            return null;
        }
        //终端号数组
        byte[] terminalNumArr = new byte[6];
        msg.readBytes(terminalNumArr);
        //终端号(去除前面的0)
        String terminalNumber = StringUtils.stripStart(ByteBufUtil.hexDump(terminalNumArr), "0");
        //消息流水号
        int msgFlowId = msg.readUnsignedShort();
        //消息总包数
        int packetTotalCount = 0;
        //包序号
        int packetOrder = 0;
        //分包
        if (multiPacket) {
            packetTotalCount = msg.readShort();
            packetOrder = msg.readShort();
        }
        //消息体
        byte[] msgBodyArr = new byte[msgBodyLen];
        msg.readBytes(msgBodyArr);
        if(msgId==0x0200) {
            //解析消息体
            LocationData locationData=parseLocationBody(Unpooled.wrappedBuffer(msgBodyArr));
            locationData.setIndex(msgFlowId);
            locationData.setDataLength(msgBodyLen);
            //校验码
            int checkCode = msg.readUnsignedByte();
            //包尾
            msg.readByte();
            //获取消息回复内容
            String replyMsg= PacketUtil.replyBinaryMessage(terminalNumArr,msgFlowId);
            //定义定位数据实体类
            Result model = new Result();
            model.setDeviceID(terminalNumber);
            model.setMsgType("Location");
            model.setDataBody(locationData);
            model.setReplyMsg(replyMsg);
            return model;
        }else {
            //定义定位数据实体类
            Result model = new Result();
            model.setDeviceID(terminalNumber);
            model.setMsgType("heartbeat");
            model.setDataBody(null);
            return model;
        }
    }

    /**
     * 解析指令数据
     * @param in 原始数据
     * @return
     */
    public static Result decodeTextMessage(ByteBuf in) {

        //读指针设置到消息头
        in.markReaderIndex();
        //查找消息尾，如果未找到则继续等待下一包
        int tailIndex = in.bytesBefore(Constant.TEXT_MSG_TAIL);
        if (tailIndex < 0) {
            in.resetReaderIndex();
            return null;
        }
        //定义定位数据实体类
        Result model = new Result();
        //包头(
        in.readByte();
        //字段列表
        List<String> itemList = new ArrayList<String>();
        while (in.readableBytes() > 0) {
            //查询逗号的下标截取数据
            int index = in.bytesBefore(Constant.TEXT_MSG_SPLITER);
            int itemLen = index > 0 ? index : in.readableBytes() - 1;
            byte[] byteArr = new byte[itemLen];
            in.readBytes(byteArr);
            in.readByte();
            itemList.add(new String(byteArr));
        }
        String msgType = "";
        if (itemList.size() >= 5) {
            msgType = itemList.get(3) + itemList.get(4);
        }
        Object dataBody=null;
        if(itemList.size()>0){
            dataBody="(";
            for(String item :itemList) {
                dataBody+=item+",";
            }
            dataBody=CommonUtil.trimEnd(dataBody.toString(),",");
            dataBody += ")";
        }
        String replyMsg="";
        if(msgType.equals("BASE2")&&itemList.get(5).toUpperCase().equals("TIME")) {
            replyMsg=PacketUtil.replyBASE2Message(itemList);
        }
        model.setDeviceID(itemList.get(0));
        model.setMsgType(msgType);
        model.setDataBody(dataBody);
        model.setReplyMsg(replyMsg);
        return model;
    }

    /**
     * 解析定位消息体
     * @param msgBodyBuf
     * @return
     */
    private static LocationData parseLocationBody(ByteBuf msgBodyBuf){
        //报警标志
        long alarmFlag = msgBodyBuf.readUnsignedInt();
        //状态
        long status = msgBodyBuf.readUnsignedInt();
        //纬度
        double lat = NumberUtil.multiply(msgBodyBuf.readUnsignedInt(), NumberUtil.COORDINATE_PRECISION);
        //经度
        double lon = NumberUtil.multiply(msgBodyBuf.readUnsignedInt(), NumberUtil.COORDINATE_PRECISION);
        //海拔高度,单位为米
        int altitude = msgBodyBuf.readShort();
        //速度
        double speed = NumberUtil.multiply(msgBodyBuf.readUnsignedShort(), NumberUtil.ONE_PRECISION);
        //方向
        int direction = msgBodyBuf.readShort();
        //定位时间
        byte[] timeArr = new byte[6];
        msgBodyBuf.readBytes(timeArr);
        String bcdTimeStr = ByteBufUtil.hexDump(timeArr);
        ZonedDateTime gpsZonedDateTime = CommonUtil.parseBcdTime(bcdTimeStr);
        //根据状态位的值判断是否南纬和西经
        if (NumberUtil.getBitValue(status, 2) == 1) {
            lat = -lat;
        }
        if (NumberUtil.getBitValue(status, 3) == 1) {
            lon = -lon;
        }
        //定位状态
        int locationType=NumberUtil.getBitValue(status, 18);
        if(locationType==0)
        {
            locationType = NumberUtil.getBitValue(status, 1);
        }
        if(locationType==0)
        {
            locationType = NumberUtil.getBitValue(status, 6) > 0 ? 2 : 0;
        }
        //锁绳状态
        int lockRope=NumberUtil.getBitValue(status, 20);
        //锁电机状态
        int lockMotor=NumberUtil.getBitValue(status, 21);
        //锁状态(由锁绳/按钮+电机来组合判断，当锁绳/按钮为开（1）则锁状态必然为开（1）；或者电机状态为开（1）则锁状态也为开（1）；其他的则为关（0）)
        int lockStatus=0;
        if(lockRope==1||lockMotor==1) {
            lockStatus=1;
        }
        int backCover=NumberUtil.getBitValue(status, 7);
        //唤醒源
        long awaken = (status>>24)&0b00001111;
        int alarm=parseAlarm(alarmFlag);
        LocationData locationData=new LocationData();
        locationData.setGpsTime(gpsZonedDateTime.toString());
        locationData.setLatitude(lat);
        locationData.setLongitude(lon);
        locationData.setLocationType(locationType);
        locationData.setSpeed((int)speed);
        locationData.setDirection(direction);
        locationData.setAltitude(altitude);
        locationData.setLockStatus(lockStatus);
        locationData.setLockRope(lockRope);
        locationData.setAwaken((int)awaken);
        locationData.setBackCover(backCover);
        locationData.setAlarm(alarm);
        //处理附加信息
        if (msgBodyBuf.readableBytes() > 0) {
            parseExtraInfo(msgBodyBuf, locationData);
        }
        return locationData;
    }

    /**
     * 解析附加信息
     *
     * @param msgBody
     * @param location
     */
    private static void parseExtraInfo(ByteBuf msgBody, LocationData location) {
        ByteBuf extraInfoBuf = null;
        while (msgBody.readableBytes() > 1) {
            int extraInfoId = msgBody.readUnsignedByte();
            int extraInfoLen = msgBody.readUnsignedByte();
            if (msgBody.readableBytes() < extraInfoLen) {
                break;
            }
            extraInfoBuf = msgBody.readSlice(extraInfoLen);
            switch (extraInfoId) {
                //锁事件
                case 0x0B:
                    LockEvent event=new LockEvent();
                    //锁事件
                    int type=extraInfoBuf.readUnsignedByte();
                    event.setType(type);
                    if(type==0x01||type==0x02||type==0x03||type==0x05||type==0x1E||type==0x1F){
                        //凭密开锁
                        byte[] passwordArr = new byte[6];
                        extraInfoBuf.readBytes(passwordArr);
                        String password=new String(passwordArr);
                        event.setPassword(password);
                        int unlockStatus=extraInfoBuf.readUnsignedByte();
                        if(unlockStatus==0xff){
                            event.setUnLockStatus(0);
                        }else{
                            if(unlockStatus>0&&unlockStatus<100){
                                event.setFenceId(unlockStatus);
                            }
                            event.setUnLockStatus(1);
                        }
                    }else if(type==0x06||type==0x07||type==0x08||type==0x10||type==0x11||type==0x18||type==0x19||type==0x20||type==0x28||type==0x29){
                        //凭密开锁
                        byte[] passwordArr = new byte[6];
                        extraInfoBuf.readBytes(passwordArr);
                        String password=new String(passwordArr);
                        event.setPassword(password);
                        event.setUnLockStatus(0);
                    }else if(type==0x22){
                        //卡号
                        long cardId = extraInfoBuf.readUnsignedInt();
                        if(cardId!=0) {
                            event.setCardNo(String.format("%010d", cardId));
                        }
                        if(extraInfoBuf.readableBytes()>0) {
                            int unlockStatus = extraInfoBuf.readUnsignedByte();
                            if (unlockStatus == 0xff) {
                                event.setUnLockStatus(0);
                            } else {
                                if (unlockStatus > 0 && unlockStatus < 100) {
                                    event.setFenceId(unlockStatus);
                                }
                                event.setUnLockStatus(1);
                            }
                        }else{
                            event.setUnLockStatus(1);
                        }
                    }else if(type==0x23||type==0x2A||type==0x2B){
                        //卡号
                        long cardId = extraInfoBuf.readUnsignedInt();
                        if(cardId!=0) {
                            event.setCardNo(String.format("%010d", cardId));
                        }
                    }
                    location.setLockEvent(event);
                    break;
                //无线通信网络信号强度
                case 0x30:
                    int fCellSignal=extraInfoBuf.readByte();
                    location.setGSMSignal(fCellSignal);
                    break;
                //卫星数
                case 0x31:
                    int fGPSSignal=extraInfoBuf.readByte();
                    location.setGpsSignal(fGPSSignal);
                    break;
                //电池电量百分比
                case 0xD4:
                    int fBattery=extraInfoBuf.readUnsignedByte();
                    location.setBattery(fBattery);
                    break;
                //电池电压
                case 0xD5:
                    int fVoltage=extraInfoBuf.readUnsignedShort();
                    location.setVoltage(fVoltage*0.01);
                    break;
                case 0xF9:
                    //版本号
                    int version=extraInfoBuf.readUnsignedShort();
                    location.setProtocolVersion(version);
                    break;
                case 0xFD:
                    //小区码信息
                    int mcc=extraInfoBuf.readUnsignedShort();
                    location.setMCC(mcc);
                    int mnc=extraInfoBuf.readUnsignedByte();
                    location.setMNC(mnc);
                    long cellId=extraInfoBuf.readUnsignedInt();
                    location.setCELLID((int)cellId);
                    int lac=extraInfoBuf.readUnsignedShort();
                    location.setLAC(lac);
                    break;
                case 0xFC:
                    int fenceId = extraInfoBuf.readUnsignedByte();
                    location.setFenceId(fenceId);
                    break;
                case 0xFE:
                    long mileage = extraInfoBuf.readUnsignedInt();
                    location.setMileage(mileage);
                    break;
                default:
                    ByteBufUtil.hexDump(extraInfoBuf);
                    break;
            }
        }
    }

    /**
     * 报警解析
     * @param alarmFlag
     * @return
     */
    private static int parseAlarm(long alarmFlag) {
        int alarm=-1;
        //单次触发报警
        if(NumberUtil.getBitValue(alarmFlag, 1) == 1)
        {
            alarm = Integer.parseInt(AlarmTypeEnum.ALARM_1.getValue());
        }else if(NumberUtil.getBitValue(alarmFlag, 7) == 1)
        {
            alarm = Integer.parseInt(AlarmTypeEnum.ALARM_2.getValue());
        }else if(NumberUtil.getBitValue(alarmFlag, 16) == 1)
        {
            alarm = Integer.parseInt(AlarmTypeEnum.ALARM_3.getValue());
        }else if(NumberUtil.getBitValue(alarmFlag, 17) == 1)
        {
            alarm = Integer.parseInt(AlarmTypeEnum.ALARM_4.getValue());
        }else if(NumberUtil.getBitValue(alarmFlag, 18) == 1)
        {
            alarm = Integer.parseInt(AlarmTypeEnum.ALARM_5.getValue());
        }
        return alarm;
    }
}
