package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class RegistryException extends RuntimeException{
    public RegistryException(Exception e){
        super(e);
    }
    public RegistryException(String s){
        super(s);
    }
}
