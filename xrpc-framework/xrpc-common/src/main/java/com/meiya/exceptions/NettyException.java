package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class NettyException extends RuntimeException{
    public NettyException(){}
    public NettyException(String s){
        super(s);
    }
    public NettyException(Throwable e){
        super(e);
    }
}
