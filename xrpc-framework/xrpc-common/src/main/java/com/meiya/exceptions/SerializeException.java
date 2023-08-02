package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String s){
        super(s);
    }
    public SerializeException(Throwable e){
        super(e);
    }
}
