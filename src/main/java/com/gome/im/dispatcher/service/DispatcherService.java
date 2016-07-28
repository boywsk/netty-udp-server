package com.gome.im.dispatcher.service;


import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.ServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.response.RspServersByCmdMsg;
import com.gome.im.dispatcher.model.response.RspServersMsg;
import com.gome.im.dispatcher.mongo.ServerDao;
import com.gome.im.dispatcher.process.DispatchProcess;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class DispatcherService {
    private static Logger LOG = LoggerFactory.getLogger(DispatcherService.class);
    private static ScheduledExecutorService SCHDULE_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private final static int FIVE_MINUTES = 5 * 60 * 1000; //客户端汇报时间
    private final static int ONE_MINUTES = 1 * 60 * 1000; //服务检测时间
    private static ServerDao SERVER_DAO = new ServerDao();
    public static DispatcherService INSTANCE = new DispatcherService();
    private DispatchProcess dispatchProcess = new DispatchProcess();

    public static DispatcherService getInstance() {
        return INSTANCE;
    }

    private DispatcherService() {
    }

    /**
     * 定时检测服务资源汇报状态（客户端每四分钟会汇报一次服务状态到服务器）
     */
    public void init() {
        try {
            SCHDULE_SERVICE.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
                        Map<String, String> map = cluster.hgetAll(Global.REDIS_SERVERS_KEY);
                        long nowTime = System.currentTimeMillis();
                        if (map != null) {
                            Set<Map.Entry<String, String>> set = map.entrySet();
                            for (Map.Entry<String, String> entry : set) {
                                ServerModel server = JSON.parseObject(entry.getValue(), ServerModel.class);
                                long lastUpdateTime = server.getUpdateTime();
                                //客户端每4分钟一定会汇报一次服务
                                if (nowTime - lastUpdateTime > (FIVE_MINUTES + ONE_MINUTES)) {
                                    String ipPort = entry.getKey();
                                    //redis del
                                    cluster.hdel(Global.REDIS_SERVERS_KEY, ipPort);

                                    //mongo update status
                                    server.setStatus(Global.SERVER_STATUS.NONE.value);
                                    SERVER_DAO.saveOrUpdateServer(server);
                                    LOG.error("服务状况检测 --> 1. 存在服务没有汇报状态,请检查服务状态,服务地址:{},服务类型:{}", ipPort, server.getType());
                                }
                            }
                            if (set.isEmpty()) {
                                LOG.error("服务状况检测 --> 2. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查各服务,定时及时汇报状态!");
                            }
                            LOG.info("服务状况检测 --> 3. 当前调度服务所有服务资源内容:{}", JSON.toJSONString(map));
                        } else {
                            LOG.error("服务状况检测 --> 4. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查......");
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOG.error("error:{}", e);
                    }
                }
            }, FIVE_MINUTES + ONE_MINUTES, ONE_MINUTES, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("dispatcher scheduleService error:{}", e);
        }
    }

    /**
     * 客户端每5分钟一定要汇报一次服务，否则服务器会作为服务宕掉处理
     *
     * @param type
     * @param cmd
     * @param ipPort
     */
    public static void report(int type, Set<Long> cmd, String ipPort) {
        long nowTime = System.currentTimeMillis();
        ServerModel server = new ServerModel();
        server.setStatus(Global.SERVER_STATUS.OK.value);
        server.setType(type);
        server.setCmd(cmd);
        if (StringUtils.isEmpty(ipPort)) {
            LOG.error("错误数据:客户端汇报资源的ipPort值为空:{}", ipPort);
            return;
        }
        server.setIpPort(ipPort);
        server.setUpdateTime(nowTime);
        try {
            //存入redis
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            cluster.hset(Global.REDIS_SERVERS_KEY, server.getIpPort(), JSON.toJSONString(server));
            //存入mongodb
            SERVER_DAO.saveOrUpdateServer(server);
            LOG.info("report success,ipPort:{},type:{}", server.getIpPort(), server.getType());
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("dispatcher report error:{},type:{},ipPort:{}", e, server.getType(), server.getIpPort());
        }
    }

    /**
     * 客户端拉取服务器列表
     *
     * @param type
     * @return
     */
    public static RspServersMsg getServersByType(int type) {
        RspServersMsg rsp = null;
        JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
        Map<String, String> map = cluster.hgetAll(Global.REDIS_SERVERS_KEY);
        if (map != null) {
            rsp = new RspServersMsg();
            List<String> ipPortList = new ArrayList<>();

            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
                ServerModel server = JSON.parseObject(entry.getValue(), ServerModel.class);
                if (server.getType() == type) {
                    String ipPort = entry.getKey();
                    ipPortList.add(ipPort);
                }
            }
            rsp.setIpPort(ipPortList);
            rsp.setType(type);
            LOG.info("getServersByType success,ipPorts:{},type:{}", ipPortList.toString(), type);
        }
        return rsp;
    }

    /**
     * 客户端拉取服务器列表
     *
     * @param cmd
     * @return
     */
    public static RspServersMsg getServersByCmd(Set<Long> cmd) {
        RspServersMsg rsp = null;
        JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
        Map<String, String> map = cluster.hgetAll(Global.REDIS_SERVERS_KEY);
        if (map != null) {
            rsp = new RspServersMsg();
            List<RspServersByCmdMsg> rspServersByCmdMsgList = new ArrayList<>();

            //获取与命令字匹配的服务器资源
            for (Long command : cmd) {
                RspServersByCmdMsg rspServersByCmdMsg = null;
                List<String> ipPortList = new ArrayList<>();
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    ServerModel server = JSON.parseObject(entry.getValue(), ServerModel.class);
                    Set<Long> serverCmds = server.getCmd();
                    if (serverCmds != null && serverCmds.contains(command)) {
                        String ipPort = entry.getKey();
                        ipPortList.add(ipPort);
                    }
                }
                if (ipPortList.size() > 0) {
                    rspServersByCmdMsg = new RspServersByCmdMsg();
                    rspServersByCmdMsg.setCmd(command);
                    rspServersByCmdMsg.setIpPort(ipPortList);

                    rspServersByCmdMsgList.add(rspServersByCmdMsg);
                }
            }
            rsp.setRspServers(rspServersByCmdMsgList);
            LOG.info("getServersByCmd success,rsp:{}", JSON.toJSONString(rsp));
        }
        return rsp;
    }

    /**
     * 逻辑处理
     *
     * @param ctx
     * @param packet
     * @param json
     */
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, String json) {
        ClientMsg msg = JSON.parseObject(json, ClientMsg.class);
        dispatchProcess.process(ctx, packet, msg);
    }

}
