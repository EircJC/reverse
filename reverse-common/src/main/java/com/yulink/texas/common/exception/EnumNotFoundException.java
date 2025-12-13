package com.yulink.texas.common.exception;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2018/11/20
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public class EnumNotFoundException extends RuntimeException{
    public EnumNotFoundException() {
        super();
    }

    public EnumNotFoundException(String message) {
        super(message);
    }

}
