package com.system;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * @author LinShaoJun
 * @Date 2020/1/14 0:43
 */
@Data
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Response<T> implements Serializable {

    private static final long serialVersionUID = -4149726986568498605L;

    /**
     * 请求成功返回码为：0000
     */
    private static final String successCode = "0000";

    /**
     * 返回数据
     */
    private T data;

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回描述
     */
    private String msg;

    public Response(){
        this.msg = "";
        this.code = successCode;
    }

    public Response(String code, String msg){
        this();
        this.code = code;
        this.msg = msg;
    }

    public Response(String code, String msg, T data){
        this();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Response(String code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <T> Response<T> getSUCCESS() {
        Response<T> response = new Response<T>();
        response.setCode("0000");
        response.setMsg("请求成功!");
        return response;
    }

    public static <T> Response<T> getSUCCESS(ResponseCode responseCode) {
        Response<T> response = new Response<T>();
        response.setCode(responseCode.getCode());
        response.setMsg(responseCode.getMsg());
        return response;
    }

    public static <T> Response<T> getSUCCESS(ResponseCode responseCode, T data) {
        Response<T> response = new Response<T>();
        response.setCode(responseCode.getCode());
        response.setMsg(responseCode.getMsg());
        response.setData(data);
        return response;
    }

    public static <T> Response<T> getFAIL(ResponseCode responseCode) {
        Response<T> response = new Response<T>();
        response.setCode(responseCode.getCode());
        response.setMsg(responseCode.getMsg());
        return response;
    }

    public static <T> Response<T> getFAIL(ResponseCode responseCode, T data) {
        Response<T> response = new Response<T>();
        response.setCode(responseCode.getCode());
        response.setMsg(responseCode.getMsg());
        response.setData(data);
        return response;
    }

    @Override
    public String toString() {
        return "RestResult{" +
                ", code='" + code + '\'' +
                ", data=" + data +
                ", msg=" + msg +
                '}';
    }
}
