package com.jointech.sdk.jt709.constants;

/**
 * 常量定义
 * @author HyoJung
 */
public class Constant {
    private Constant(){}
    /**
     * 二进制消息包头
     */
    public static final byte BINARY_MSG_HEADER = 0x7E;
    /**
     * 不包含消息体的基本长度
     */
    public static final int BINARY_MSG_BASE_LENGTH = 15;

    /**
     * 消息最大长度
     */
    public static final int BINARY_MSG_MAX_LENGTH = 102400;

    /**
     * 文本消息包头
     */
    public static final byte TEXT_MSG_HEADER = '(';

    /**
     * 文本消息包尾
     */
    public static final byte TEXT_MSG_TAIL = ')';

    /**
     * 文本消息分隔符
     */
    public static final byte TEXT_MSG_SPLITER = ',';
}