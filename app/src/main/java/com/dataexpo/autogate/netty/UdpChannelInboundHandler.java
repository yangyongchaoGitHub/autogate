package com.dataexpo.autogate.netty;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public abstract class UdpChannelInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final String TAG = UdpChannelInboundHandler.class.getSimpleName();
    private ChannelHandlerContext ctx = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (this.ctx != null) {
            this.ctx.close();
        }
    }

    public int send(Object o) {
        ctx.writeAndFlush(o).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                Log.d(TAG, "send end! ");
            }
        });
        return 1;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String body = msg.content().toString(CharsetUtil.UTF_8);
        receive(body);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        //Log.d(TAG, "channelReadComplete");
        //ctx.flush();
    }

    public abstract void receive(String data);
}
