package com.yulink.texas.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class NettyWebSocketServer implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

    private final NettyWebSocketProperties properties;

    private final NettyWebSocketHandler webSocketHandler;

    private final Object lifecycleMonitor = new Object();

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Channel serverChannel;

    private volatile boolean started;

    public NettyWebSocketServer(NettyWebSocketProperties properties, NettyWebSocketHandler webSocketHandler) {
        this.properties = properties;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        start();
    }

    public void start() {
        synchronized (lifecycleMonitor) {
            if (started) {
                log.info("Netty WebSocket server already started");
                return;
            }
            if (!properties.isEnabled()) {
                log.info("Netty WebSocket server is disabled by configuration");
                return;
            }
            bossGroup = new NioEventLoopGroup(properties.getBossThreads());
            workerGroup = new NioEventLoopGroup(properties.resolveWorkerThreads());
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, properties.getBacklog())
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, properties.isKeepAlive())
                        .childOption(ChannelOption.TCP_NODELAY, properties.isTcpNoDelay())
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel channel) {
                                channel.pipeline()
                                        .addLast(new HttpServerCodec())
                                        .addLast(new ChunkedWriteHandler())
                                        .addLast(new HttpObjectAggregator(properties.getMaxContentLength()))
                                        .addLast(new IdleStateHandler(
                                                properties.getReaderIdleSeconds(),
                                                properties.getWriterIdleSeconds(),
                                                properties.getAllIdleSeconds(),
                                                TimeUnit.SECONDS));
                                if (properties.isCompression()) {
                                    channel.pipeline().addLast(new WebSocketServerCompressionHandler());
                                }
                                channel.pipeline()
                                        .addLast(new WebSocketServerProtocolHandler(
                                                properties.normalizedPath(),
                                                null,
                                                true,
                                                properties.getMaxFramePayloadLength()))
                                        .addLast(new WebSocketFrameAggregator(properties.getMaxFramePayloadLength()))
                                        .addLast(webSocketHandler);
                            }
                        });
                ChannelFuture bindFuture = StringUtils.hasText(properties.getHost())
                        ? bootstrap.bind(properties.getHost(), properties.getPort()).sync()
                        : bootstrap.bind(properties.getPort()).sync();
                serverChannel = bindFuture.channel();
                started = true;
                serverChannel.closeFuture().addListener(future -> log.info("Netty WebSocket server channel closed"));
                log.info("Netty WebSocket server started, address=ws://{}:{}{}",
                        resolveHostForLog(),
                        properties.getPort(),
                        properties.normalizedPath());
            } catch (InterruptedException e) {
                shutdownQuietly();
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Netty WebSocket server startup interrupted", e);
            } catch (Exception e) {
                shutdownQuietly();
                throw new IllegalStateException("Failed to start Netty WebSocket server", e);
            }
        }
    }

    public void stop() {
        synchronized (lifecycleMonitor) {
            if (!started && serverChannel == null && bossGroup == null && workerGroup == null) {
                return;
            }
            started = false;
            Channel channel = serverChannel;
            serverChannel = null;
            if (channel != null) {
                channel.close().syncUninterruptibly();
            }
            shutdownQuietly();
            log.info("Netty WebSocket server stopped");
        }
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void destroy() {
        stop();
    }

    private void shutdownQuietly() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup = null;
        }
    }

    private String resolveHostForLog() {
        return StringUtils.hasText(properties.getHost()) ? properties.getHost() : "0.0.0.0";
    }
}
