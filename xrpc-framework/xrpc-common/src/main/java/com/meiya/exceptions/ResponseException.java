package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class ResponseException extends RuntimeException{
    public ResponseException(){}
    public ResponseException(Throwable e){
        super(e);
    }
    public ResponseException(String s){
        super(s);
    }
}
