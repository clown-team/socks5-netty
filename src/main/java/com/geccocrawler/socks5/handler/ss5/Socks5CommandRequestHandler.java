package com.geccocrawler.socks5.handler.ss5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {
    private final EventLoopGroup bossGroup;

    public Socks5CommandRequestHandler(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
        }

        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            if (log.isTraceEnabled()) {
                log.trace("准备连接目标服务器");
            }

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new LoggingHandler());//in out
                            //将目标服务器信息转发给客户端
                            ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
                        }
                    });

            if (log.isTraceEnabled()) {
                log.trace("连接目标服务器");
            }

            ChannelFuture future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
            future.addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    clientChannelContext.pipeline().addLast(new Client2DestHandler(future1));
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
                    clientChannelContext.writeAndFlush(commandResponse);
                } else {
                    Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
                    clientChannelContext.writeAndFlush(commandResponse);
                }
            });
        } else {
            clientChannelContext.fireChannelRead(msg);
        }
    }

    /**
     * 将目标服务器信息转发给客户端
     *
     * @author huchengyi
     */
    private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {

        private final ChannelHandlerContext clientChannelContext;

        public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
            this.clientChannelContext = clientChannelContext;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
            clientChannelContext.writeAndFlush(destMsg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
            clientChannelContext.channel().close();
        }
    }

    /**
     * 将客户端的消息转发给目标服务器端
     *
     * @author huchengyi
     */
    private static class Client2DestHandler extends ChannelInboundHandlerAdapter {

        private final ChannelFuture destChannelFuture;

        public Client2DestHandler(ChannelFuture destChannelFuture) {
            this.destChannelFuture = destChannelFuture;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            destChannelFuture.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            destChannelFuture.channel().close();
        }
    }
}
