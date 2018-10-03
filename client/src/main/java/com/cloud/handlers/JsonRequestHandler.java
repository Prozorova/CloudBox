package com.cloud.handlers;

import com.cloud.utils.JSONHelper;
import com.cloud.utils.jsonQueries.StandardJsonQuery;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonRequestHandler extends MessageToByteEncoder<StandardJsonQuery>{

	@Override
	protected void encode(ChannelHandlerContext ctx, StandardJsonQuery msg, ByteBuf out) throws Exception {
		out.writeBytes(JSONHelper.writeJson(msg));
	}
}
