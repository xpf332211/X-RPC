package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class ZookeeperException extends RuntimeException{
    public ZookeeperException(Exception e) {
        super(e);
    }
}
