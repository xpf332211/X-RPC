package com.meiya.enumeration;

/**
 * @author xiaopf
 */
public enum ResponseCode {
    /**
     * 请求成功状态码
     */
    SUCCESS((byte) 1,"成功"),
    /**
     * 请求失败状态码
     */
    FAIL((byte) 2,"异常");
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
