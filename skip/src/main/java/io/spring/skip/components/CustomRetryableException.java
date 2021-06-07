package io.spring.skip.components;

public class CustomRetryableException extends Exception {

    public CustomRetryableException() {
        super();
    }

    public CustomRetryableException(String message) {
        super(message);
    }

}
