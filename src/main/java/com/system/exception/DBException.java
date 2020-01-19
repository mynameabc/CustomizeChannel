package com.system.exception;

/**
 * @author LinShaoJun
 * @Date 2020/1/15 17:58
 */
public class DBException extends BaseException {

    public DBException(){
        super("数据库异常!");
    }

    public DBException(String errorMsg) {
        super(errorMsg, new Throwable());
    }

    public DBException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
    }
}
