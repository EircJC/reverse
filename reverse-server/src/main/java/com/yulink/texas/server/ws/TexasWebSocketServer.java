package com.yulink.texas.server.ws;

import com.alibaba.fastjson.JSON;
import com.yulink.texas.core.domain.SystemLog;
import com.yulink.texas.server.common.entity.BaseEntity;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.common.utils.TexasStatic;
import com.yulink.texas.server.manager.PlayerManager;
import com.yulink.texas.server.manager.RoomManager;
import com.yulink.texas.server.manager.SkillManager;
import com.yulink.texas.server.ws.model.ChatMsg;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Author: chao.jiang
 * @Date: 2022/9/7
 * @Copyright (c) bitmain.com All Rights Reserved
 */

@ServerEndpoint("/ws/texas")
@Component
@Slf4j
public class TexasWebSocketServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, TexasWebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 牌桌Id
     */
    private String tableId = "";

    private String userName = "";

    // 缓冲区最大大小
    static final int maxSize = 1024;// 1 * 1024;// 1K

    @OnOpen
    public void onOpen(Session session) {
        log.info("onOpen");
        // 可以缓冲的传入二进制消息的最大长度
        session.setMaxBinaryMessageBufferSize(maxSize);
        // 可以缓冲的传入文本消息的最大长度
        session.setMaxTextMessageBufferSize(maxSize);
        System.out.println(Method.class.getName() +" sessionId:"+session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        onConnectLost(session);
        log.info(" connection closed ");
    }

    @OnError
    public void onError(Session session, Throwable e) {
        onConnectLost(session);
        log.info(" connection error: " + e.getMessage());
        e.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        log.info("onMessage:" + message);
//		 onMessageDoReflect(message, session);
        System.out.println(Method.class.getName() +" sessionId:"+session.getId());
        onMessageDo(message, session);
    }

    public void onMessageDo(String message, Session session) {
        BaseEntity be = JsonUtils.fromJson(message, BaseEntity.class);
        int action = be.getAction();
        try {
            switch (action) {
                case 0:// 注册
                    ((PlayerManager) SpringUtil.getBean("playerManager")).register(session, message);
                    break;
                case 1:// 登录
                    ((PlayerManager) SpringUtil.getBean("playerManager")).login(session, message);
                    break;
                case 2:// 2进入房间
                    ((RoomManager) SpringUtil.getBean("roomManager")).inRoom(session, message);
                    break;
                case 3:// 3退出房间
                    ((RoomManager) SpringUtil.getBean("roomManager")).outRoom(session, message);
                    break;
                case 4:// 4坐下
                    ((PlayerManager) SpringUtil.getBean("playerManager")).sitDown(session, message);
                    break;
                case 5:// 5站起
                    ((PlayerManager) SpringUtil.getBean("playerManager")).standUp(session, message);
                    break;
                case 6:// 6过牌
                    ((PlayerManager) SpringUtil.getBean("playerManager")).check(session, message);
                    break;
                case 7:// 7下注
                    ((PlayerManager) SpringUtil.getBean("playerManager")).betChips(session, message);
                    break;
                case 8:// 8弃牌
                    ((PlayerManager) SpringUtil.getBean("playerManager")).fold(session, message);
                    break;
//                case 9:// 9获取排行榜
//                    ((LobbyService) SpringUtil.getBean("lobbyService")).getRankList(session, message);
//                    break;
                case 10:// 使用技能
                    ((SkillManager) SpringUtil.getBean("skillManager")).useSkill(session, message);
                    break;
                case 11:// 补充筹码
                    ((PlayerManager) SpringUtil.getBean("playerManager")).assignChipsNum(session, message);

            }

        } catch (Exception e) {
            e.printStackTrace();
//            SystemLogService syslogService = (SystemLogService) SpringUtil.getBean("SystemLogServiceImpl");
            SystemLog entity = new SystemLog();
            entity.setType(action + "");
            entity.setOperation(message);
            StackTraceElement[] eArray = e.getCause().getStackTrace();
            String errorMessage = "";
            for (int i = 0; i < eArray.length; i++) {
                String className = e.getCause().getStackTrace()[i].getClassName();
                String MethodName = e.getCause().getStackTrace()[i].getMethodName();
                int LineNumber = e.getCause().getStackTrace()[i].getLineNumber();
                errorMessage = errorMessage + "\n---" + className + "." + MethodName + ",line:" + LineNumber;
            }
            entity.setContent(e.getCause() + errorMessage);
            entity.setDatetime(new Date());
//            syslogService.insertSystemLog(entity);
            String retMsg = "{\"c\":\"onException\",\"state\":0,\"message\":\"系统异常" + errorMessage + "\"}";
            sendText(session, retMsg);
            log.info(e.getCause() + errorMessage);
        }
    }

    public void onConnectLost(Session session) {
        // Todo 将来保留断线重连机制，逻辑重新梳理
        PlayerVO p = TexasStatic.loginPlayerMap.get(session.getId());
        // 从登录玩家列表中移除玩家信息
        if (p != null && p.getRoom() != null) {
            RoomManager roomManager = (RoomManager) SpringUtil.getBean("roomManager");
            roomManager.outRoom(session, "", false);
        }
        TexasStatic.loginPlayerMap.remove(session.getId());
        TexasStatic.playerSessionMap.remove(p.getId());
    }



//    /**
//     * @Description: 收到客户端消息后调用的方法, 调用API接口 发送消息到
//     * @params: [message, session]
//     * @return: void
//     * @Author: wangxianlin
//     * @Date: 2020/5/9 9:13 PM
//     */
//    @OnMessage
//    public void onMessage(String message, @PathParam("userName") String userName) {
//        log.info("用户消息:" + userName + ",报文:" + message);
//        if (StringUtils.isNotBlank(message)) {
//            try {
//                //解析发送的报文
//                JSONObject jsonObject = JSON.parseObject(message);
//                //追加发送人(防止串改)
//                jsonObject.put("sender", this.userName);
//                String receiver = jsonObject.getString("receiver");
//                //传送给对应toUserId用户的websocket
//                if (StringUtils.isNotBlank(receiver) && webSocketMap.containsKey(receiver)) {
//                    webSocketMap.get(receiver).session.getBasicRemote().sendText(jsonObject.toJSONString());
//                } else {
//                    log.error("用户:" + receiver + "不在该服务器上");
//                    //否则不在这个服务器上，发送到mysql或者redis
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 发布websocket消息
     * 消息格式： { "sender": "u2","receiver": "u1","msg": "hello world","createTime":"2021-10-12 11:12:11"}
     *
     * @param dto
     * @return
     */
    public static void sendWebsocketMessage(ChatMsg dto) {
        if (dto != null) {
            if (StringUtils.isNotBlank(dto.getReceiver()) && webSocketMap.containsKey(dto.getReceiver())) {
                String json = JSON.toJSONString(dto);
                try {
                    webSocketMap.get(dto.getReceiver()).session.getBasicRemote().sendText(json);
                } catch (IOException e) {
                    log.error("消息发送异常：{}", e.toString());
                }
            } else {
                log.error("用户:" + dto.getReceiver() + ",不在线！");
            }
        }
    }


    /**
     * @Description: 获取在线人数
     * @params: []
     * @return: int
     * @Author: wangxianlin
     * @Date: 2020/5/9 9:09 PM
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * @Description: 在线人数+1
     * @params: []
     * @return: void
     * @Author: wangxianlin
     * @Date: 2020/5/9 9:09 PM
     */
    public static synchronized void addOnlineCount() {
        TexasWebSocketServer.onlineCount++;
    }

    /**
     * @Description: 在线人数-1
     * @params: []
     * @return: void
     * @Author: wangxianlin
     * @Date: 2020/5/9 9:09 PM
     */
    public static synchronized void subOnlineCount() {
        TexasWebSocketServer.onlineCount--;
    }

    /**
     * 发送文本消息
     *
     * @param session
     * @param text
     */
    public static void sendText(Session session, String text) {
        if (session == null) {
            return;
        }
        synchronized (session) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(text);
                    // log.info(text);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
