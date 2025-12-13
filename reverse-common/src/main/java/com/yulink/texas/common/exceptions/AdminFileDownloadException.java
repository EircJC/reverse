package com.yulink.texas.common.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminFileDownloadException extends BusinessException {

    public AdminFileDownloadException(String code, String msg) {
        super(code, msg);
    }

    public AdminFileDownloadException(String msg) {
        super(msg);
    }

}
