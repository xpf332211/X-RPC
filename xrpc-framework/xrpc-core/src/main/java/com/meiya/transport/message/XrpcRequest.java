package com.meiya.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaopf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XrpcRequest {
    /**
     * 请求id
     */
    private long requestId;
    /**
     * 压缩类型
     */
    private byte compressType;
    /**
     * 序列化方式
     */
    private byte serializeType;
    /**
     * 请求类型
     */
    private byte requestType;
    /**
     * 请求负载
     */
    private RequestPayload requestPayload;


}
