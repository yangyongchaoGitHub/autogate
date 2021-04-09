package com.dataexpo.autogate.netty;
import android.util.Log;

import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.JsonUtil;
import com.dataexpo.autogate.listener.OnFrameCallback;
import com.dataexpo.autogate.model.Frame;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class UDPClient extends UdpChannelInboundHandler implements Runnable {
    private final static String TAG = UDPClient.class.getSimpleName();
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private UdpChannelInitializer udpChannelInitializer;
    private ExecutorService executorService;
    private ArrayList<OnFrameCallback> callbacks = new ArrayList<>();

    public UDPClient(){
        init();
    }

    private void init(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioDatagramChannel.class)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));;
                //.option(ChannelOption.SO_RCVBUF,10240);
//                .option(ChannelOption.SO_SNDBUF,1024);
        udpChannelInitializer = new UdpChannelInitializer(this);
        bootstrap.handler(udpChannelInitializer);

        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this);
    }

    public void setOnFrameCallback(OnFrameCallback onFrameCallback) {
        boolean bExist = false;
        for (OnFrameCallback o: callbacks) {
            if (o == onFrameCallback) {
                bExist = true;
                break;
            }
        }
        if (!bExist) {
            callbacks.add(onFrameCallback);
        }
    }

    public void deleteFrameCallback(OnFrameCallback onFrameCallback) {
        for (int i = 0; i < callbacks.size(); i++) {
            if (callbacks.get(i) == onFrameCallback) {
                callbacks.remove(i);
            }
        }
    }

    @Override
    public void receive(String data) {
        //Log.d(TAG, "client rcv size : " + data.length());
//        Log.d(TAG, "client rcv : " + data);
        //Log.d(TAG, "callbacks size : " + callbacks.size());

        Frame frame = null;
        try {
            frame = JsonUtil.getInstance().json2obj(data, Frame.class);
        } catch (Exception e) {
            e.printStackTrace();
            frame = null;
        }

        if (frame != null) {
            for (OnFrameCallback o: callbacks) {
                o.onFrame(FileUtils.base64ToBytes(frame.data));
            }
        }
    }

    @Override
    public void run() {
        try {
            ChannelFuture channelFuture = bootstrap.bind(19123).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public void sendTo() {
        send(new DatagramPacket(Unpooled.copiedBuffer("echo", CharsetUtil.UTF_8),
                new InetSocketAddress("192.168.1.17",19123)));
    }
}
