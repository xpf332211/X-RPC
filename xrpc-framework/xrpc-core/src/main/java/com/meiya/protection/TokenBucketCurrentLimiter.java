package com.meiya.protection;
import lombok.extern.slf4j.Slf4j;


/**
 * 令牌桶限流器
 * @author xiaopf
 */
@Slf4j
public class TokenBucketCurrentLimiter {

    /**
     * 令牌桶
     */
    private int tokens;

    /**
     * 令牌最大数
     */
    private final int maxToken;
    /**
     * 令牌填充速率
     */
    private final int fillRate;
    /**
     * 上一次填充令牌的时间
     */
    private long lastRefillTime;

    public TokenBucketCurrentLimiter(int maxToken, int fillRate) {
        this.maxToken = maxToken;
        this.fillRate = fillRate;
        this.tokens = maxToken;
        this.lastRefillTime = System.currentTimeMillis();
    }


    /**
     * 判断请求是否可以放行
     * 需要保证线程安全
     * @return true放行 反之
     */
    public synchronized boolean allowRequestPass(){
        //计算两次请求的时间间隔内 需要添加的令牌
        long currentTime = System.currentTimeMillis();
        int filledTokens = (int) ((currentTime - lastRefillTime) * fillRate / 1000L);
        //请求过于频繁则不添加令牌
        int currentLimitingTime = 1000;
        if ((currentTime - lastRefillTime) > currentLimitingTime){
            tokens = Math.min(maxToken,tokens + filledTokens);
            lastRefillTime = currentTime;
        }
        //处理请求
        if (tokens >= 1){
            tokens --;
            return true;
        }else {
            return false;
        }
    }

    public static void main(String[] args) {

        //一秒内 10个请求通过 剩余的请求失败
        TokenBucketCurrentLimiter limiter = new TokenBucketCurrentLimiter(10, 10);
        for (int i = 0;i < 100;i++){
            try {
                //10ms发送一次请求 每秒接收100个请求
                Thread.sleep(10);
                boolean pass = limiter.allowRequestPass();
                if (pass){
                    log.debug("请求通过");
                }else {
                    log.debug("请求被拦截");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
