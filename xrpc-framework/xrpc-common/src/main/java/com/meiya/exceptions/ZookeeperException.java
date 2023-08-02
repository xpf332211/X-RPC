package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class ZookeeperException extends RuntimeException{
    public ZookeeperException(){}
    public ZookeeperException(Throwable e) {
        super(e);
    }
    public ZookeeperException(String s){
        super(s);
    }
}
