package com.cloud.server.handlers;

import java.io.IOException;

import com.cloud.utils.JSONHelper;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class JacksonJsonHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			JSONHelper.readToFile(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ctx.writeAndFlush("OK!").addListener(ChannelFutureListener.CLOSE);

	}
	
	@Override
    public  void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();	
        ctx.close();
    }

}
