package com.meiya.hook;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author xiaopf
 */
public class ShutDownHolder {
    /**
     * 挡板
     */
    public static final AtomicBoolean BAFFLE = new AtomicBoolean(false);

    /**
     * 请求计数器
     */
    public static final LongAdder REQUEST_COUNTER = new LongAdder();
}
