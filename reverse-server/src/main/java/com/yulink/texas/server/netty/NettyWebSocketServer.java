package com.yulink.texas.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyWebSocketServer {

    @Value("${netty.websocket.port:8080}")
    private int port;

    @Value("${netty.websocket.path:/ws/texas}")
    private String websocketPath;

    @Autowired
    private NettyWebSocketHandler webSocketHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // HTTP编解码
                                    .addLast(new HttpServerCodec())
                                    // 大数据流支持
                                    .addLast(new ChunkedWriteHandler())
                                    // HTTP消息聚合
                                    .addLast(new HttpObjectAggregator(65536))
                                    // WebSocket压缩
                                    .addLast(new WebSocketServerCompressionHandler())
                                    // WebSocket协议升级
                                    .addLast(new WebSocketServerProtocolHandler(websocketPath, null, true))
                                    // 空闲检测
                                    .addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS))
                                    // 自定义WebSocket处理器
                                    .addLast(webSocketHandler);
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            log.info("Netty WebSocket Server started on port: {}", port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("Netty WebSocket Server stopped");
    }
}