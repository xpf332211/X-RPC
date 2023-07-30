package com.meiya.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应体
 * @author xiaopf
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseBody implements Serializable {
    /**
     * 响应内容
     */
    private Object responseContext;
}
