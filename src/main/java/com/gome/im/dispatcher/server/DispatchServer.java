package com.gome.im.dispatcher.server;

import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.handler.UdpServerHandler;
import com.gome.im.dispatcher.service.DispatcherService;
import com.gome.im.dispatcher.utils.ZKClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.Executors;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class DispatchServer {
    private static Logger LOG = LoggerFactory.getLogger(DispatchServer.class);

    private int port;

    private Channel channel;

    public DispatchServer(int port) {
        this.port = port;
    }

    public void init() throws Exception {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("decoder", new JsonObjectDecoder());
                            p.addLast(new UdpServerHandler());
                        }
                    });
            InetAddress address = InetAddress.getLocalHost();
            channel = b.bind(address, port).sync().channel();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        group.shutdownGracefully();
                    }
                }
            });
            LOG.info("UDP服务器启动, host:{},port:{}", address.getHostAddress(), port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //启动服务
        int serverPort = 8866;
        //可以接受端口参数
        if(args.length >= 1 && args[0] != null){
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        //启动服务
        DispatchServer server = new DispatchServer(serverPort);
        server.init();


        final int finalServerPort = serverPort;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //初始化ZK,并将服务地址发布到ZK根节点 "/im-dispatcher" 的子节点
                ZKClient.getInstance().init(Global.ZK_IP_PORT,finalServerPort,Global.ZK_PATH);
            }
        });

        //初始化客户端服务状态汇报检测
        DispatcherService.getInstance().init();

        LOG.info("服务器开启success......,端口号:{}",serverPort);
    }
}
