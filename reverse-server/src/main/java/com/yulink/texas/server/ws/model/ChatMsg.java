package com.yulink.texas.server.ws.model;

import java.util.Date;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/7
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class ChatMsg {

//    { "sender": "u2","receiver": "u1","msg": "hello world","createTime":"2021-10-12 11:12:11"}
    private String sender;
    private String receiver;
    private String msg;
    private Date createTime;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
