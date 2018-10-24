package com.cloud;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cloud.fx.Controller;
import com.cloud.fx.SceneManager.Scenes;
import com.cloud.handlers.ClientChannelInboundHandlerAdapter;
import com.cloud.handlers.ClientMessageDecoder;
import com.cloud.utils.handlers.TransferMessageEncoder;
import com.cloud.utils.processors.StandardTransference;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Клиент Netty
 * @author prozorova 08.10.2018
 */
public class CloudBoxClient {
	
	private static final int  INET_PORT_NUMBER = 8189;
	private static final String      INET_HOST = "localhost";
	
	private Channel currentChannel = null;
	
	private static CloudBoxClient INSTANCE = null;
	
	private static final ExecutorService service = Executors.newFixedThreadPool(5);
	
	private CloudBoxClient() {}
	
	public static CloudBoxClient getInstance() {
		if (INSTANCE == null || INSTANCE.currentChannel == null || !INSTANCE.currentChannel.isActive()) {
			INSTANCE = new CloudBoxClient();
			Thread t = new Thread(() -> {
				try {
					INSTANCE.start();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			t.setDaemon(true); 
			service.submit(t);
		}
		return INSTANCE;
	}
    
    /**
     * Соединение с сервером
     * @throws InterruptedException
     */
	public void start() throws InterruptedException  {
		
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
//                                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                                    pipeline.addLast(new ClientMessageDecoder());
//                                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                    pipeline.addLast(new TransferMessageEncoder());
                                    pipeline.addLast(new ClientChannelInboundHandlerAdapter());
                                }
                           });
			
			ChannelFuture future = clientBootstrap.remoteAddress(new InetSocketAddress(INET_HOST, INET_PORT_NUMBER))
                                                  .connect()
                                                  .sync();
			
			future.channel().closeFuture().sync();

        } finally {
        	workerGroup.shutdownGracefully();
        }
	}
	
	/**
	 * Отправка сообщения на сервер
	 * @param data сообщение
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void sendData(StandardTransference data) throws IOException, InterruptedException {
		
		if (INSTANCE.currentChannel == null || !INSTANCE.currentChannel.isActive()) {
			// если нет открытого канала - переходим на экран аутотентификации
			Controller.getSceneManager().changeScene(Scenes.AUTH);
			Controller.throwAlertMessage("Error", "You are unauthorized or connection to server lost");
			return;
		}
		Thread t = new Thread(() -> {
				INSTANCE.currentChannel.writeAndFlush(data);
		});
		t.setDaemon(true);
		service.submit(t);
	}
	
	public void disconnect() {
		currentChannel.close();
	}

}
