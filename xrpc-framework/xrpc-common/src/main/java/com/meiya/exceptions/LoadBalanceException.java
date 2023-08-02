package com.meiya.exceptions;

/**
 * @author xiaopf
 */
public class LoadBalanceException extends RuntimeException{
    public LoadBalanceException() {
        super();
    }

    public LoadBalanceException(String message) {
        super(message);
    }

    public LoadBalanceException(Throwable cause) {
        super(cause);
    }
}
