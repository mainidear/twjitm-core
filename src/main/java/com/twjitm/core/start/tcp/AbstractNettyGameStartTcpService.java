package com.twjitm.core.start.tcp;

import com.twjitm.core.common.factory.thread.ThreadNameFactory;
import com.twjitm.core.spring.SpringServiceManager;
import com.twjitm.core.start.AbstractNettyGameStartService;
import com.twjitm.core.utils.logs.LoggerUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;


/**
 * @author �Ľ�
 * @date 2018/4/16
 */

public abstract class AbstractNettyGameStartTcpService extends AbstractNettyGameStartService {
    private static Logger logger = LoggerUtils.getLogger(AbstractNettyGameStartTcpService.class);
    private int serverPort;
    private String serverIp;

    private ThreadNameFactory bossThreadNameFactory;
    private ThreadNameFactory workerThreadNameFactory;
    private ChannelInitializer channelInitializer;

    private EventLoopGroup listenIntoGroup;
    private EventLoopGroup progressGroup;

    public AbstractNettyGameStartTcpService(int serverPort, String serverIp, String bossTreadName, String workerTreadName, ChannelInitializer channelInitializer) {
        super(serverPort, new InetSocketAddress(serverIp, serverPort));
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.bossThreadNameFactory = new ThreadNameFactory(bossTreadName);
        this.workerThreadNameFactory = new ThreadNameFactory(workerTreadName);
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void startServer() {
        listenIntoGroup = new NioEventLoopGroup(1, bossThreadNameFactory);
        progressGroup = new NioEventLoopGroup(0, workerThreadNameFactory);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(listenIntoGroup, progressGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture;
        try {
            channelFuture = bootstrap.bind(this.serverIp, this.serverPort).sync();
            logger.info("[---------------------TCP SERVICE START IS SUCCESSFUL IP=[" + serverIp + "]LISTENER PORT NUMBER IS :[" + serverPort + "]------------]");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("TCP START HAVE ERROR ,WILL STOP");
            SpringServiceManager.shutdown();
            e.printStackTrace();
            logger.error(e);
        } finally {
            listenIntoGroup.shutdownGracefully();
            progressGroup.shutdownGracefully();
            logger.info("SERVER WORLD STOP");
        }
    }

    @Override
    public void stopServer() throws Throwable {
        listenIntoGroup.shutdownGracefully();
        progressGroup.shutdownGracefully();
    }

}