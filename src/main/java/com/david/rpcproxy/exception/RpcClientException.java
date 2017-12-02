package com.david.rpcproxy.exception;

public class RpcClientException extends RuntimeException {
    private Integer code;

    public RpcClientException(Integer code,String message, Throwable cause) {
        super(message, cause);
        this.code=code;
    }

    public RpcClientException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public RpcClientException(String message) {
        super(message);
    }

    public RpcClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
