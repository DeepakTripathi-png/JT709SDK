package com.jointech.sdk.jt709.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 锁事件
 * @author HyoJung
 */
@Data
public class LockEvent {
    /**
     * 事件类型
     */
    @JSONField(name = "Type")
    public int Type;
    /**
     * 刷卡卡号
     */
    @JSONField(name = "CardNo")
    public String CardNo;
    /**
     * 开锁密码
     */
    @JSONField(name = "Password")
    public String Password;
    /**
     * 开锁状态(1:成功；0：失败)
     */
    @JSONField(name = "UnLockStatus")
    public int UnLockStatus=0;
    /**
     * 与开锁有关的围栏ID
     */
    @JSONField(name = "FenceId")
    public int FenceId=-1;
}
