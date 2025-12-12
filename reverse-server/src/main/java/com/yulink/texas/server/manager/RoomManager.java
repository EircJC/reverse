package com.yulink.texas.server.manager;

import com.yulink.texas.server.ws.TexasUtil;
import javax.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/8
 * @Copyright (c) bitmain.com All Rights Reserved
 */
@Component
@Slf4j
public class RoomManager {

    public void inRoom(Session session, String message) {
        TexasUtil.inRoom(session, message);
    }

    public void outRoom(Session session, String message, boolean sendOrNot) {
        TexasUtil.outRoom(session, message, sendOrNot);
    }

    public void outRoom(Session session, String message) {
        TexasUtil.outRoom(session, message, true);
    }

}
