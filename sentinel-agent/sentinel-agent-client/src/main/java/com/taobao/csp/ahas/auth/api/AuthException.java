package com.taobao.csp.ahas.auth.api;
import com.taobao.csp.ahas.transport.api.RequestException;

public class AuthException extends RequestException {
    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }
}
