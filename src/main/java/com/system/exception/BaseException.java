package com.system.exception;

/**
 * @author LinShaoJun
 * @Date 2020/1/15 20:08
 */
public class BaseException extends RuntimeException {

    public BaseException(String errorMsg) {
        super(errorMsg, new Throwable(errorMsg));
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
    }
}
