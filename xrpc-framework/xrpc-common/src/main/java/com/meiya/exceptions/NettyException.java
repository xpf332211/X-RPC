package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class NettyException extends RuntimeException{
    public NettyException(String s){
        super(s);
    }
    public NettyException(Exception e){
        super(e);
    }
}
