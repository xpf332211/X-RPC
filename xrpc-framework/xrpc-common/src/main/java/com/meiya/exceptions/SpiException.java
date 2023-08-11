package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class SpiException extends Exception{
    public SpiException() {
        super();
    }

    public SpiException(String message) {
        super(message);
    }

    public SpiException(Exception e) {
        super(e);
    }
}
