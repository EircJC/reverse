package com.yulink.texas.common.web.result;

import com.yulink.texas.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2020/5/14
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DefaultSuccessResult implements Result {



    private static final long serialVersionUID = 874200365941306385L;

    private String code;

    private String msg;

    private Object data;

    public static DefaultSuccessResult success() {
        DefaultSuccessResult result = new DefaultSuccessResult();
        result.setResultCode(ErrorCode.SUCCESS);
        return result;
    }

    public static DefaultSuccessResult success(Object data) {
        DefaultSuccessResult result = new DefaultSuccessResult();
        result.setResultCode(ErrorCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static DefaultSuccessResult failure(ErrorCode resultCode) {
        DefaultSuccessResult result = new DefaultSuccessResult();
        result.setResultCode(resultCode);
        return result;
    }

    public static DefaultSuccessResult failure(ErrorCode resultCode, Object data) {
        DefaultSuccessResult result = new DefaultSuccessResult();
        result.setResultCode(resultCode);
        result.setData(data);
        return result;
    }

    public static DefaultSuccessResult failure(String message) {
        DefaultSuccessResult result = new DefaultSuccessResult();
        result.setCode(ErrorCode.PARAM_IS_INVALID.getCode());
        result.setMsg(message);
        return result;
    }

    private void setResultCode(ErrorCode code) {
        this.code = code.getCode();
        this.msg = code.getMsg();
    }

}