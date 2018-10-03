package com.cloud.server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.cloud.server.handlers.JacksonJsonHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class CloudBoxServer {

	private static final Logger logger = Logger.getLogger(CloudBoxServer.class);
	
	private static final int  INET_PORT_NUMBER = 8189;
	private static final String      INET_HOST = "localhost";	

	public void start() throws Exception {
		BasicConfigurator.configure();
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
			               .channel(NioServerSocketChannel.class)
					       .childHandler(new ChannelInitializer<SocketChannel>() {
								@Override
								public void initChannel(SocketChannel socketChannel) {
									ChannelPipeline pipeline = socketChannel.pipeline();
									pipeline.addLast(new ObjectEncoder());
									pipeline.addLast(new JsonObjectDecoder());
									pipeline.addLast(new JacksonJsonHandler());
								}
							})
							.bind(INET_HOST, INET_PORT_NUMBER)
				            .sync()
				            .channel()
				            .closeFuture()
				            .syncUninterruptibly();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
	
	
	public static void main(String[] args) throws Exception {
        new CloudBoxServer().start();
    }
}
