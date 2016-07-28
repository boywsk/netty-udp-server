package com.gome.im.dispatcher.handler;

import com.gome.im.dispatcher.service.DispatcherService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private DispatcherService dispatcherService = DispatcherService.getInstance();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf buf = packet.content();
        int length = buf.readableBytes();
        byte[] msgBytes = new byte[length];
        buf.readBytes(msgBytes);
        String msg = new String(msgBytes, CharsetUtil.UTF_8);
        logger.info("服务端收到消息,消息内容:{}", msg);
        try {
            dispatcherService.process(ctx, packet, msg);
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error("服务器内部错误,客户端消息内容:{}", msg);
        }
    }
}
