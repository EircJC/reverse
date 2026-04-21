package com.yulink.texas.server.netty;

import com.yulink.texas.server.common.entity.BaseEntity;
import com.yulink.texas.server.common.entity.PlayerVO;
import com.yulink.texas.server.common.utils.JsonUtils;
import com.yulink.texas.server.common.utils.TexasStatic;
import com.yulink.texas.server.manager.PlayerManager;
import com.yulink.texas.server.manager.RoomManager;
import com.yulink.texas.server.manager.SkillManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Sharable
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<Boolean> DISCONNECT_HANDLED =
            AttributeKey.valueOf("texas.disconnect.handled");

    private final PlayerManager playerManager;

    private final RoomManager roomManager;

    private final SkillManager skillManager;

    @Autowired
    public NettyWebSocketHandler(PlayerManager playerManager, RoomManager roomManager, SkillManager skillManager) {
        this.playerManager = playerManager;
        this.roomManager = roomManager;
        this.skillManager = skillManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        handleDisconnect(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String message = ((TextWebSocketFrame) frame).text();
            log.info("Received message: {}", message);
            handleMessage(ctx.channel(), message);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            return;
        }
        if (frame instanceof CloseWebSocketFrame) {
            handleDisconnect(ctx.channel());
            ctx.close();
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            sendMessage(ctx.channel(), "{\"c\":\"onException\",\"state\":0,\"message\":\"暂不支持二进制消息\"}");
            return;
        }
        log.warn("Unsupported websocket frame type: {}", frame.getClass().getSimpleName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
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
                return;
            }
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(new PingWebSocketFrame());
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void handleMessage(Channel channel, String message) {
        BaseEntity baseEntity;
        try {
            baseEntity = JsonUtils.fromJson(message, BaseEntity.class);
        } catch (Exception e) {
            log.warn("Invalid websocket message: {}", message, e);
            sendMessage(channel, "{\"c\":\"onException\",\"state\":0,\"message\":\"消息格式不正确\"}");
            return;
        }
        if (baseEntity == null) {
            sendMessage(channel, "{\"c\":\"onException\",\"state\":0,\"message\":\"消息不能为空\"}");
            return;
        }
        int action = baseEntity.getAction();
        try {
            switch (action) {
                case 0:
                    playerManager.register(channel, message);
                    break;
                case 1:
                    playerManager.login(channel, message);
                    break;
                case 2:
                    roomManager.inRoom(channel, message);
                    break;
                case 3:
                    roomManager.outRoom(channel, message);
                    break;
                case 4:
                    playerManager.sitDown(channel, message);
                    break;
                case 5:
                    playerManager.standUp(channel, message);
                    break;
                case 6:
                    playerManager.check(channel, message);
                    break;
                case 7:
                    playerManager.betChips(channel, message);
                    break;
                case 8:
                    playerManager.fold(channel, message);
                    break;
                case 10:
                    skillManager.useSkill(channel, message);
                    break;
                case 11:
                    playerManager.assignChipsNum(channel, message);
                    break;
                case 12:
                    roomManager.getRoomLevelStats(channel, message);
                    break;
                case 13:
                    roomManager.getRoomList(channel, message);
                    break;
                case 14:
                    roomManager.inRoomByRoomNo(channel, message);
                    break;
                case 15:
                    roomManager.createRoomAndIn(channel, message);
                    break;
                default:
                    sendMessage(channel, "{\"c\":\"onException\",\"state\":0,\"message\":\"未知指令\"}");
                    log.warn("Unsupported action: {}, message: {}", action, message);
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendMessage(channel, "{\"c\":\"onException\",\"state\":0,\"message\":\"系统异常\"}");
        }
    }

    private void handleDisconnect(Channel channel) {
        Boolean disconnectHandled = channel.attr(DISCONNECT_HANDLED).get();
        if (Boolean.TRUE.equals(disconnectHandled)) {
            return;
        }
        channel.attr(DISCONNECT_HANDLED).set(Boolean.TRUE);
        String channelId = channelId(channel);
        PlayerVO player = TexasStatic.loginPlayerMap.get(channelId);
        if (player != null && player.getRoom() != null) {
            try {
                roomManager.outRoom(channel, "", false);
            } catch (Exception e) {
                log.warn("Error while removing player from room on disconnect, channelId={}", channelId, e);
            }
        }
        TexasStatic.loginPlayerMap.remove(channelId);
        if (player != null) {
            TexasStatic.playerSessionMap.remove(player.getId(), channelId);
        }
    }

    public static void sendMessage(Channel channel, String message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(message))
                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    private String channelId(Channel channel) {
        return channel.id().asShortText();
    }
}
