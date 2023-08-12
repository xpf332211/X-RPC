package com.meiya.protection;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器
 * @author xiaopf
 */
@Slf4j
public class CircuitBreaker {
    /**
     * 熔断门限阈值 (错误数)
     */
    private final int threshold;
    /**
     * 熔断恢复时间 ms (恢复后 开->半开)
     */
    private final int recoveryTime;
    private final AtomicInteger failureCount;
    private long lastFailureTime;
    private State currentState;

    public CircuitBreaker(int threshold, int recoveryTime) {
        this.threshold = threshold;
        this.recoveryTime = recoveryTime;
        this.failureCount = new AtomicInteger(0);
        this.currentState = State.CLOSED;
    }

    public boolean allowRequestPass() {
        //断路器打开
        if (currentState == State.OPEN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFailureTime >= recoveryTime) {
                //达到了恢复时间 变为半开 等待请求进入 尝试开或关
                currentState = State.HALF_OPEN;
                failureCount.set(0);
                log.debug("达到熔断恢复时间,关闭态-->半开态");
            } else {
                log.debug("当前熔断器开启态");
                return false;
            }
        }
        //断路器关闭或半关
        int currentCount = failureCount.get();
        if (currentCount >= threshold) {
            currentState = State.OPEN;
            lastFailureTime = System.currentTimeMillis();
            log.debug("错误率达到门限阈值,关闭态-->开启态");
            return false;
        }
        log.debug("当前熔断器关闭态");
        return true;
    }

    /**
     * 请求失败
     */
    public void reportFailure() {
        //当前断路器关闭 若错误数大于门限值 将断路器打开
        if (currentState == State.CLOSED) {
            int currentFailureCount = failureCount.incrementAndGet();
            if (currentFailureCount >= threshold) {
                currentState = State.OPEN;
                lastFailureTime = System.currentTimeMillis();
                log.debug("错误率达到门限阈值,关闭态-->开启态");
            }
        }
        //当前断路器半开 由于请求失败 继续打开断路器
        else if (currentState == State.HALF_OPEN) {
            currentState = State.OPEN;
            lastFailureTime = System.currentTimeMillis();
            log.debug("放入一个请求,请求失败,半开态-->开启态");
        }
    }

    /**
     * 请求成功
     */
    public void reportSuccess() {
        //当前断路器半开 由于请求成功 断路器关闭
        if (currentState == State.HALF_OPEN) {
            currentState = State.CLOSED;
            log.debug("放入一个请求,请求成功,半开态-->关闭态");
        }
    }

    private enum State {
        OPEN, HALF_OPEN, CLOSED
    }

    public static void main(String[] args) {
        CircuitBreaker breaker = new CircuitBreaker(3, 10);
        for (int i = 0;i < 1000;i++){
            int num = new Random().nextInt(100);
            if (num > 90){
                breaker.reportFailure();
            }else {
                breaker.reportSuccess();
            }
            breaker.allowRequestPass();
        }
    }
}
