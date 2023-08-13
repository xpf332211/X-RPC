package com.meiya.enumeration;

/**
 * 成功码 20(方法调用成功) 21(心跳返回成功)
 * 限流码 31(服务器负载过高，被限流)
 * 错误码 44(客户端错误，请求的资源不存在)  50(服务端错误，服务器内部错误) 51(目标服务器正在关闭中)
 * @author xiaopf
 */
public enum ResponseCode {
    /**
     * 方法调用状态码
     */
    SUCCESS((byte) 20,"方法调用成功"),
    /**
     * 心跳返回成功状态码
     */
    SUCCESS_HEART_BEAT((byte) 21,"心跳返回成功"),
    /**
     * 服务器限流状态码
     */
    CURRENT_LIMIT((byte) 31,"请求被限流"),
    /**
     * 客户端错误状态码
     */
    CLIENT_FAIL((byte) 44,"请求资源不存在"),
    /**
     * 服务端错误状态码
     */
    SERVER_ERROR((byte) 50,"服务器内部错误"),
    /**
     * 服务端关闭状态码
     */
    SERVER_CLOSING((byte) 51,"目标服务器正在关闭中");
    private final byte code;
    private final String desc;

    ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
