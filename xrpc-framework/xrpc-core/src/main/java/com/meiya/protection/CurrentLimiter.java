package com.meiya.protection;

/**
 * @author xiaopf
 */
public interface CurrentLimiter {
    /**
     * 判断请求是否被限流
     * @return 是否限流
     */
    boolean allowRequestPass();
}
