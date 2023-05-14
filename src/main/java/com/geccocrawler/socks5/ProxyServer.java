package com.geccocrawler.socks5;

import com.geccocrawler.socks5.auth.PasswordAuth;
import com.geccocrawler.socks5.auth.impl.EnvPasswordAuth;
import com.geccocrawler.socks5.handler.ChannelListener;
import com.geccocrawler.socks5.handler.ss5.Socks5CommandRequestHandler;
import com.geccocrawler.socks5.handler.ss5.Socks5InitialRequestHandler;
import com.geccocrawler.socks5.handler.ss5.Socks5PasswordAuthRequestHandler;
import com.geccocrawler.utils.CommonUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务器
 *
 * @author smilex
 */
@Slf4j
@Data
public class ProxyServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();

    private final int port;

    private boolean auth;

    private ChannelListener channelListener;

    private PasswordAuth passwordAuth;

    private ProxyServer(int port) {
        this.port = port;
    }

    public static ProxyServer create(int port) {
        return new ProxyServer(port);
    }

    public ProxyServer auth(boolean auth) {
        this.auth = auth;
        return this;
    }

    public ProxyServer channelListener(ChannelListener channelListener) {
        this.channelListener = channelListener;
        return this;
    }

    public ProxyServer passwordAuth(PasswordAuth passwordAuth) {
        this.passwordAuth = passwordAuth;
        return this;
    }

    public ChannelListener getChannelListener() {
        return channelListener;
    }

    public PasswordAuth getPasswordAuth() {
        return passwordAuth;
    }

    public boolean isAuth() {
        return auth;
    }

    public void start() throws Exception {
        if (passwordAuth == null) {
            passwordAuth = EnvPasswordAuth.getInstance();
        }

        final EventLoopGroup boss = new NioEventLoopGroup(CommonUtils.getEnv("bossThread", Integer::parseInt, 2));
        final EventLoopGroup worker = new NioEventLoopGroup(CommonUtils.getEnv("bossThread", Integer::parseInt, 0));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, CommonUtils.getEnv("backlog", Integer::parseInt, 1024))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CommonUtils.getEnv("connectTimeoutMillis", Integer::parseInt, 1000))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //Socks5 Message ByteBuf
                            ch.pipeline().addLast(Socks5ServerEncoder.DEFAULT);
                            //sock5 init
                            ch.pipeline().addLast(new Socks5InitialRequestDecoder());
                            //sock5 init
                            ch.pipeline().addLast(new Socks5InitialRequestHandler(ProxyServer.this));
                            if (isAuth()) {
                                //socks auth
                                ch.pipeline().addLast(new Socks5PasswordAuthRequestDecoder());
                                //socks auth
                                ch.pipeline().addLast(new Socks5PasswordAuthRequestHandler(getPasswordAuth()));
                            }
                            //socks connection
                            ch.pipeline().addLast(new Socks5CommandRequestDecoder());
                            //Socks connection
                            ch.pipeline().addLast(new Socks5CommandRequestHandler(ProxyServer.this.getBossGroup()));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("bind port : " + port);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        final Integer port = CommonUtils.getEnv("port", Integer::parseInt, 11080);
        final Boolean auth = CommonUtils.getEnv("auth", Boolean::valueOf, Boolean.FALSE);

        ProxyServer.create(port)
                .auth(auth)
                .start();
    }
}
