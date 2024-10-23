package com.jointech.sdk.jt709.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 结果实体类
 * @author HyoJung
 */
@Data
public class Result implements Serializable {
    @JSONField(name = "DeviceID")
    private String DeviceID;
    @JSONField(name = "MsgType")
    private String MsgType;
    @JSONField(name = "DataBody")
    private Object DataBody;
    @JSONField(name = "ReplyMsg")
    private String ReplyMsg;
}
