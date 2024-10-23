package com.jointech.sdk.jt709.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>Description: 定位实体类</p>
 *
 * @author lenny
 * @version 1.0.1
 */
@Data
public class LocationData implements Serializable {
    /**
     * 消息体
     */
    @JSONField(name = "DataLength")
    public int DataLength;
    /**
     * 定位时间
     */
    @JSONField(name = "GpsTime")
    public String GpsTime;
    /**
     * 纬度
     */
    @JSONField(name = "Latitude")
    public double Latitude;
    /**
     * 经度
     */
    @JSONField(name = "Longitude")
    public double Longitude;
    /**
     * 定位方式
     */
    @JSONField(name = "LocationType")
    public int LocationType;
    /**
     * 速度
     */
    @JSONField(name = "Speed")
    public int Speed;
    /**
     * 方向
     */
    @JSONField(name = "Direction")
    public int Direction;
    /**
     * 里程
     */
    @JSONField(name = "Mileage")
    public long Mileage;
    /**
     * 海拔高度
     */
    @JSONField(name = "Altitude")
    public int Altitude;
    /**
     * GPS信号值
     */
    @JSONField(name = "GpsSignal")
    public int GpsSignal;
    /**
     * GSM信号质量
     */
    @JSONField(name = "GSMSignal")
    public int GSMSignal;
    /**
     * 电量值
     */
    @JSONField(name = "Battery")
    public int Battery;
    /**
     * 电压
     */
    @JSONField(name = "Voltage")
    public double Voltage;
    /**
     * 锁状态
     */
    @JSONField(name = "LockStatus")
    public int LockStatus;
    /**
     * 锁绳状态
     */
    @JSONField(name = "LockRope")
    public int LockRope;
    /**
     * 后盖状态
     */
    @JSONField(name = "BackCover")
    public int BackCover;
    /**
     * 协议版本号
     */
    @JSONField(name = "ProtocolVersion")
    public int ProtocolVersion;
    /**
     * 围栏ID
     */
    @JSONField(name = "FenceId")
    public int FenceId;
    /**
     * MCC
     */
    @JSONField(name = "MCC")
    public int MCC;
    /**
     * MNC
     */
    @JSONField(name = "MNC")
    public int MNC;
    /**
     * LAC
     */
    @JSONField(name = "LAC")
    public int LAC;
    /**
     * CELLID
     */
    @JSONField(name = "CELLID")
    public long CELLID;
    /**
     * Awaken
     */
    @JSONField(name = "Awaken")
    public int Awaken;
    /**
     * Alarm
     */
    @JSONField(name = "Alarm")
    public int Alarm;
    /**
     * 锁事件
     */
    @JSONField(name = "LockEvent")
    public LockEvent LockEvent;
    /**
     * 流水号
     */
    @JSONField(name = "Index")
    public int Index;
}
