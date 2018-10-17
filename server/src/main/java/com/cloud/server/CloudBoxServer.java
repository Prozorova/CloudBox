package com.cloud.server;

import org.apache.log4j.Logger;

import com.cloud.server.handlers.ServerMessageDecoder;
import com.cloud.server.handlers.TransferMessageHandler;
import com.cloud.utils.handlers.TransferMessageEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Сервер Netty
 * @author prozorova 10.10.2018
 */
public class CloudBoxServer {

	private static final Logger logger = Logger.getLogger(CloudBoxServer.class);
	
	private static final int  INET_PORT_NUMBER = 8189;
	private static final String      INET_HOST = "localhost";	

	public void start() throws Exception {
		
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
									pipeline.addLast(new ProtobufVarint32FrameDecoder());
									pipeline.addLast(new ServerMessageDecoder());
									pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
									pipeline.addLast(new TransferMessageEncoder());
									pipeline.addLast(new TransferMessageHandler());
								}
							})
					       .childOption(ChannelOption.SO_KEEPALIVE, true)
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
