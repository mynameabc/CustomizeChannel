package com.system;

/**
 * @author LinShaoJun
 * @Date 2020/1/14 0:52
 */
public enum ResponseCode {

    /**
     * ----------------------------系统级异常----------------------------------
     */

    /**
     * 执行成功.
     */
    EXECUTE_SUCCESS("0000", "执行成功!"),
    /**
     * 执行失败.
     */
    EXECUTE_FAIL("0001", "执行失败!"),
    /**
     * 查询成功.
     */
    QUERY_SUCCESS("0002", "查询成功!"),
    /**
     * 查询失败.
     */
    QUERY_FAIL("0003", "查询失败!"),
    /**
     * 未知异常.
     */
    UnknownException("0004", "未知异常"),
    /**
     * 数据库操作异常.
     */
    DBException("0005", "数据库操作异常!");

    /**
     * ----------------------------业务级异常----------------------------------
     */

    private String code;
    private String msg;

    ResponseCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static ResponseCode getByCode(String code) {
        for (ResponseCode ec : ResponseCode.values()) {
            if (ec.getCode().equals(code)) { return ec;}
        }
        return null;
    }
}
