package com.meiya;

import com.meiya.annotation.Retry;

/**
 * @author xiaopf
 */
public interface MessageService {

    /**
     * 获取message
     * @param name
     * @return
     */
    @Retry
    public String getMessage(String name);
}
