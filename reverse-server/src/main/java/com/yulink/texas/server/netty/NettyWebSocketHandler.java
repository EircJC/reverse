package com.yulink.texas.server.netty;

import com.yulink.texas.server.common.entity.BaseEntity;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.common.utils.TexasStatic;
import com.yulink.texas.server.manager.PlayerManager;
import com.yulink.texas.server.manager.RoomManager;
import com.yulink.texas.server.manager.SkillManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Sharable  // 添加@Sharable注解，允许Handler被多次添加到不同的ChannelPipeline中
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final PlayerManager playerManager;
    private final RoomManager roomManager;
    private final SkillManager skillManager;

    // 使用构造函数注入依赖
    @Autowired
    public NettyWebSocketHandler(PlayerManager playerManager, RoomManager roomManager, SkillManager skillManager) {
        this.playerManager = playerManager;
        this.roomManager = roomManager;
        this.skillManager = skillManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected: {}", ctx.channel().remoteAddress());
        // 可以在这里添加连接成功的处理逻辑
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        handleDisconnect(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String message = frame.text();
        log.info("Received message: {}", message);
        handleMessage(ctx.channel(), message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in WebSocket handler", cause);
        handleDisconnect(ctx.channel());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Client idle timeout, closing connection: {}", ctx.channel().remoteAddress());
                handleDisconnect(ctx.channel());
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void handleMessage(Channel channel, String message) {
        BaseEntity be = JsonUtils.fromJson(message, BaseEntity.class);
        int action = be.getAction();
        
        try {
            switch (action) {
                case 0:// 注册
                    ((PlayerManager) SpringUtil.getBean("playerManager")).register(channel, message);
                    break;
                case 1:// 登录
                    playerManager.login(channel, message);
                    break;
                case 2:// 2进入房间
                    roomManager.inRoom(channel, message);
                    break;
                case 3:// 3退出房间
                    roomManager.outRoom(channel, message);
                    break;
                case 4:// 4坐下
                    playerManager.sitDown(channel, message);
                    break;
                case 5:// 5站起
                    playerManager.standUp(channel, message);
                    break;
                case 6:// 6过牌
                    playerManager.check(channel, message);
                    break;
                case 7:// 7下注
                    playerManager.betChips(channel, message);
                    break;
                case 8:// 8弃牌
                    playerManager.fold(channel, message);
                    break;
                case 10:// 使用技能
                    skillManager.useSkill(channel, message);
                    break;
                case 11:// 补充筹码
                    playerManager.assignChipsNum(channel, message);
                    break;
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
            String errorMsg = "{\"c\":\"onException\",\"state\":0,\"message\":\"系统异常\"}";
            sendMessage(channel, errorMsg);
        }
    }

    private void handleDisconnect(Channel channel) {
        // 从登录玩家列表中移除玩家信息
        PlayerVO p = TexasStatic.loginPlayerMap.get(channel.id().asLongText());
        if (p != null && p.getRoom() != null) {
            roomManager.outRoom(channel, "", false);
        }
        TexasStatic.loginPlayerMap.remove(channel.id().asLongText());
        if (p != null) {
            TexasStatic.playerSessionMap.remove(p.getId());
        }
    }

    public static void sendMessage(Channel channel, String message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }
}