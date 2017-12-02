package com.david.rpcproxy.exception;

public class RpcProxyException extends RuntimeException {

    public RpcProxyException(String message) {
        super(message);
    }

    public RpcProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}
