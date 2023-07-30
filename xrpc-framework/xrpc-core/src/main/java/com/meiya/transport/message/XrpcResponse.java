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
public class XrpcResponse {
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
     * 响应码
     */
    private byte responseCode;
    /**
     * 响应体
     */
    private ResponseBody responseBody;
}
