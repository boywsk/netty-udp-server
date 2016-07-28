package com.gome.im.dispatcher.client;


import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.handler.UdpClientHandler;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqReportMsg;
import com.gome.im.dispatcher.model.request.ReqServersMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;


/**
 * Created by wangshikai on 2016/7/14.
 */
public class UdpClient {
    private int port;
    public Channel ch = null;
    private InetAddress address = null;

    public UdpClient(int port) {
        this.port = port;
    }

    public void init() {
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast("decoder", new JsonObjectDecoder());
                            p.addLast(new UdpClientHandler());
                        }
                    });
            ch = b.bind(port).sync().channel();
            address = InetAddress.getLocalHost();
            System.out.println("客户端地址:"+address+",客户端port:"+port);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ch.closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        group.shutdownGracefully();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ClientMsg clientMsg, String targetIp, int targetPort) {
        ByteBuf buf = Unpooled.copiedBuffer(JSON.toJSONString(clientMsg), CharsetUtil.UTF_8);
        try {
            ch.writeAndFlush(new DatagramPacket(buf, new InetSocketAddress(targetIp, targetPort)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        int localPort = 8877;
//        String serverHost = "10.69.40.90"; //本机
//        String serverHost = "10.69.16.7"; //本机
        String serverHost = "10.125.3.61"; //开发环境服务器
        int serverPort = 8866;
        for (int i = 0; i < 3; i++) {
            UdpClient client = new UdpClient(localPort + i);
            client.init();
            Set<Long> cmds = new HashSet<>();
            cmds.add(123L);
            cmds.add(456L);
            //汇报
            int requestType = 1;//请求类型:汇报
            int type =  i;
            ClientMsg reqReportMsg = createReportMsg(requestType, type, "127.0.0.1"+":"+localPort+i,cmds);
            client.sendMsg(reqReportMsg, serverHost, serverPort);

            //拉取 by type
            requestType = 2;//请求类型:拉取
            ClientMsg reqServersMsg1 = createReqServersMsg(requestType, type);
            client.sendMsg(reqServersMsg1, serverHost, serverPort);

            //拉取 by cmds
            ClientMsg reqServersMsg2 = createReqServersMsgByCmd(requestType, cmds);
            client.sendMsg(reqServersMsg2, serverHost, serverPort);
        }
    }

    //汇报服务资源消息
    public static ClientMsg createReportMsg(int requestType, int type, String ipPort,Set<Long> cmd) {
        ClientMsg clientMsg = new ClientMsg();
        clientMsg.setRequestType(requestType);
        ReqReportMsg reqReportMsg = new ReqReportMsg();
        reqReportMsg.setType(type);
        reqReportMsg.setIpPort(ipPort);
        reqReportMsg.setCmd(cmd);
        clientMsg.setReqReportMsg(reqReportMsg);
        return clientMsg;
    }

    //获取服务资源消息
    public static ClientMsg createReqServersMsg(int requestType, int type) {
        ClientMsg clientMsg = new ClientMsg();
        clientMsg.setRequestType(requestType);
        ReqServersMsg reqServersMsg = new ReqServersMsg();
        reqServersMsg.setType(type);
        clientMsg.setReqServersMsg(reqServersMsg);
        return clientMsg;
    }

    //获取服务资源消息
    public static ClientMsg createReqServersMsgByCmd(int requestType, Set<Long> cmd) {
        ClientMsg clientMsg = new ClientMsg();
        clientMsg.setRequestType(requestType);
        ReqServersMsg reqServersMsg = new ReqServersMsg();
        reqServersMsg.setCmd(cmd);
        clientMsg.setReqServersMsg(reqServersMsg);
        return clientMsg;
    }
}
