package com.meiya.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求的接口方法的描述
 * @author xiaopf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 参数类型列表
     */
    private Class<?>[] parametersType;
    /**
     * 参数名称列表
     */
    private Object[] parametersValue;
    /**
     * 返回值类型
     */
    private Class<?> returnType;
}
