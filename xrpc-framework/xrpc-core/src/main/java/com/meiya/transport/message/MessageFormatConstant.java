package com.meiya.transport.message;

import java.nio.charset.StandardCharsets;

/**
 * - magic 魔数	4字节 <br/>
 * - version 版本  1字节 <br/>
 * - header length 报文首部长度，长度单位为字节  2字节 <br/>
 * - full length  报文总长度，长度单位为字节  4字节 <br/>
 * - serializeType  序列化方式  1字节 <br/>
 * - compressType  压缩类型  1字节 <br/>
 * - requestType  请求类型  1字节 <br/>
 * - requestId请求id  8字节 <br/>
 * - requestPayload 请求体(负载)  不定长 <br/>
 * @author xiaopf
 */
public class MessageFormatConstant {
    /**
     * 魔数值
     */
    public static final byte[] MAGIC = "xrpc".getBytes(StandardCharsets.UTF_8);
    /**
     * 版本号
     */
    public static final byte VERSION = (byte) 1;
    /**
     * 报文首部长度(4+1+2+4+1+1+1+8=22)
     */
    public static final short HEADER_LENGTH = (short) 22;
    /**
     * 报文总长度字段占用的字节数
     */
    public static final short FULL_FIELD_BYTES = (short) 4;
    /**
     * 报文总长度字段的起始位置(4+1+2)
     */
    public static final short FULL_FIELD_LOCATION = (short) 7;

    /**
     * 最大帧长度
     */
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
}
