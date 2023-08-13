package com.meiya.protection;

/**
 * @author xiaopf
 */
public interface CircuitBreaker {
    /**
     * 判断请求是否应该熔断
     * @return 是否熔断
     */
    boolean allowRequestPass();

    /**
     * 记录请求失败
     */
    void reportFailure();

    /**
     * 记录请求成功
     */
    void reportSuccess();
}
