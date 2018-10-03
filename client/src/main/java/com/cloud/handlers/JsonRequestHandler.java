package com.cloud.handlers;

import com.cloud.utils.JSON;
import com.cloud.utils.JSONHelper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonRequestHandler extends MessageToByteEncoder<JSON>{

	@Override
	protected void encode(ChannelHandlerContext ctx, JSON msg, ByteBuf out) throws Exception {
		out.writeBytes(JSONHelper.writeJson(msg));
	}
}
