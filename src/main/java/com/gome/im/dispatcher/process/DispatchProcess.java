package com.gome.im.dispatcher.process;


import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.utils.LoadClassUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by wangshikai on 2016/7/27.
 */
public class DispatchProcess {

    private static Logger LOG = LoggerFactory.getLogger(DispatchProcess.class);

    private static ConcurrentMap<Integer, DispatchProcess> PROCESS_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 处理的请求类型
     */
    public int requestType;

    //初始化逻辑处理类
    static {
        try {
            Package pack = DispatchProcess.class.getPackage();
            Set<Class<?>> clazzSet = LoadClassUtil.getClasses(pack.getName());
            for (Class clz : clazzSet) {
                try {
                    DispatchProcess process = (DispatchProcess) clz.newInstance();
                    PROCESS_CLASS_MAP.put(process.requestType,process);
                } catch (InstantiationException e) {
                    LOG.error("error:{}", e);
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    LOG.error("error:{}", e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOG.error("error:{}", e);
            e.printStackTrace();
        }
    }

    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        int requestType = msg.getRequestType();
        DispatchProcess process = PROCESS_CLASS_MAP.get(requestType);
        process.process(ctx, packet, msg);
    }

    public static void main(String[] args) {
        Package pack = DispatchProcess.class.getPackage();
        Set<Class<?>> clazzSet = LoadClassUtil.getClasses(pack.getName());
        for (Class clz : clazzSet) {
            try {
                DispatchProcess process = (DispatchProcess) clz.newInstance();
                System.out.println("请求类型:" + process.requestType);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
