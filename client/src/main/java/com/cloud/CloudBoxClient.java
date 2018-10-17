package com.cloud;

import java.net.InetSocketAddress;

import com.cloud.fx.MessagesProcessor;
import com.cloud.handlers.ClientChannelInboundHandlerAdapter;
import com.cloud.handlers.ClientMessageDecoder;
import com.cloud.utils.exep.IllegalDataException;
import com.cloud.utils.handlers.TransferMessageEncoder;
import com.cloud.utils.queries.TransferMessage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Клиент Netty
 * @author prozorova 08.10.2018
 */
public class CloudBoxClient {
	
	private static final int  INET_PORT_NUMBER = 8189;
	private static final String      INET_HOST = "localhost";
	
	
	private static CloudBoxClient client = null;
	
	private CloudBoxClient() {}
	
	/**
	 * Синглтон: если объект уже создан - возвращаем его, иначе создаем
	 * @param auth данные аутентификации
	 * @param processor ссылка на обработчик сообщений
	 * @return клиент нетти
	 * @throws IllegalDataException
	 * @throws InterruptedException
	 */
    public static CloudBoxClient getCloudBoxClient(TransferMessage data, MessagesProcessor processor) 
    		throws IllegalDataException, InterruptedException {
		if (client == null) {
			client = new CloudBoxClient();
		}
		client.start(data, processor);
        return client;
    }
	
	public void start(TransferMessage data, MessagesProcessor processor) 
			throws InterruptedException, IllegalDataException  {
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap clientBootstrap = new Bootstrap()
			               .group(workerGroup)
                           .channel(NioSocketChannel.class)
                           .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel socketChannel) {
                                    ChannelPipeline pipeline = socketChannel.pipeline();
                                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                                    pipeline.addLast(new ClientMessageDecoder());
                                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                    pipeline.addLast(new TransferMessageEncoder());
                                    pipeline.addLast(new ClientChannelInboundHandlerAdapter(socketChannel, data, processor));
                                }
                           });
			ChannelFuture future = clientBootstrap.remoteAddress(new InetSocketAddress(INET_HOST, INET_PORT_NUMBER))
                                                  .connect()
                                                  .sync();
			
			future.channel().closeFuture().sync();
//			future.awaitUninterruptibly().sync();

        } finally {
        	workerGroup.shutdownGracefully();
        }
	}

}
