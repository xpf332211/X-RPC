package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class DiscoveryException extends RuntimeException{
    public DiscoveryException(Exception e){
        super(e);
    }
    public DiscoveryException(String s){
        super(s);
    }
}
