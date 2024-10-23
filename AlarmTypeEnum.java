package com.jointech.sdk.jt709.model;

import com.jointech.sdk.jt709.base.BaseEnum;
import lombok.Getter;

/**
 * 报警枚举
 * @author HyoJung
 */

public enum AlarmTypeEnum implements BaseEnum<String> {

    ALARM_1("超速报警", "1"),
    ALARM_2("低电报警", "2"),
    ALARM_3("主机开盖报警", "3"),
    ALARM_4("进围栏报警", "4"),
    ALARM_5("出围栏报警", "5");

    @Getter
    private String desc;

    private String value;

    AlarmTypeEnum(String desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static AlarmTypeEnum fromValue(Integer value) {
        String valueStr = String.valueOf(value);
        for (AlarmTypeEnum alarmTypeEnum : values()) {
            if (alarmTypeEnum.getValue().equals(valueStr)) {
                return alarmTypeEnum;
            }
        }
        return null;
    }
}
