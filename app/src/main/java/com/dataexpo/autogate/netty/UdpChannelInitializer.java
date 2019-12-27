package com.dataexpo.autogate.netty;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class UdpChannelInitializer extends ChannelInitializer<DatagramChannel> {
    private UdpChannelInboundHandler inboundHandler;
    public UdpChannelInitializer(UdpChannelInboundHandler handler){
        inboundHandler = handler;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ByteBuf delimiter = Unpooled.copiedBuffer("$_$".getBytes());
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(12,15,0));
        pipeline.addLast(inboundHandler);
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1280*720, 0, 4, 0, 4));
        //pipeline.addLast(new DelimiterBasedFrameDecoder(1383400, delimiter));
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE,
//                0, 4, 0, 4, true));
//        pipeline.addLast(new LengthFieldPrepender(ByteOrder.LITTLE_ENDIAN, 4, 0, false));
    }
}
