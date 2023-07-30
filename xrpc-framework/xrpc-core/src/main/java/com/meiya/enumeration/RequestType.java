package com.meiya.enumeration;

/**
 * 标记请求类型 requestType
 * @author xiaopf
 */

public enum RequestType {
    /**
     * 普通请求
     */
    REQUEST((byte) 1,"普通请求"),
    /**
     * 心跳检测请求
     */
    HEART_BETA((byte) 2,"心跳检测请求");
    private final byte id;
    private final String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
