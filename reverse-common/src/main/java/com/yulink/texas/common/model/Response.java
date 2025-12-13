package com.yulink.texas.common.model;

import lombok.Data;

@Data
public class Response<T> {
    /**
     * 成功时：code =  "000000"
     */
    protected String code;

    protected String msg;

    protected T data;

    public Response(){

    }

    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
