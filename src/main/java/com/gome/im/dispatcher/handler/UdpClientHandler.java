package com.gome.im.dispatcher.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by wangshikai on 2016/7/14.
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf buf = packet.content();
        int length = buf.readableBytes();
        byte[] msgBytes = new byte[length];
        buf.readBytes(msgBytes);
        String msg = new String(msgBytes,CharsetUtil.UTF_8);
        System.out.println("------------收到服务端返回消息内容-----------:"+msg);
    }
}
