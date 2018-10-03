package com.cloud;

import java.io.File;
import java.net.InetSocketAddress;

import com.cloud.handlers.JsonRequestHandler;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.jsonQueries.StandardJsonQuery;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class CloudBoxClient {
	
	private static final int  INET_PORT_NUMBER = 8189;
	private static final String      INET_HOST = "localhost";
	
	private static CloudBoxClient ourInstance = new CloudBoxClient();

    public static CloudBoxClient getInstance() {
        return ourInstance;
    }

    private CloudBoxClient() {}
    
    private Channel currentChannel;
	
	public void start(Object data) throws InterruptedException, IllegalDataException  {
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap clientBootstrap = new Bootstrap()
			               .group(workerGroup)
                           .channel(NioSocketChannel.class)
                           .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel socketChannel) {
                                	currentChannel = socketChannel;
                                    ChannelPipeline pipeline = socketChannel.pipeline();
                                    pipeline.addLast(new JsonRequestHandler());
//                                    pipeline.addLast(new ObjectEncoder());
                                    pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                    pipeline.addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        	if (data instanceof StandardJsonQuery)
                                        		sendData((StandardJsonQuery)data);
                                        	else throw new IllegalDataException(data.getClass());
                                        	
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        	String read = (String)msg;
                                            System.out.println(read);
                                        }
                                    });
                                }
                            });
			ChannelFuture future = clientBootstrap.remoteAddress(new InetSocketAddress(INET_HOST, INET_PORT_NUMBER))
                                                  .connect()
                                                  .sync();
			
//			currentChannel = future.channel();
//			currentChannel.closeFuture().syncUninterruptibly();
			future.channel().closeFuture().syncUninterruptibly();

        } finally {
//        	currentChannel.close();
        	workerGroup.shutdownGracefully();
        }
	}
	
	public void sendData(StandardJsonQuery data) {
        currentChannel.writeAndFlush(data);
    }

}
