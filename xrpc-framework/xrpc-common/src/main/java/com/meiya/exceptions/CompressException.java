package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class CompressException extends RuntimeException{
    public CompressException(){}
    public CompressException(Throwable e){
        super(e);
    }
    public CompressException(String s){
        super(s);
    }
}
