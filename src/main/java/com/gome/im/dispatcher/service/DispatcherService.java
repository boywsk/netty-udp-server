package com.gome.im.dispatcher.service;


import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.RpcServerModel;
import com.gome.im.dispatcher.model.ServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.mongo.ServerDao;
import com.gome.im.dispatcher.process.DispatchProcess;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import com.gome.im.dispatcher.utils.SMSUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    public static ServerDao SERVER_DAO = new ServerDao();
    public static DispatcherService INSTANCE = new DispatcherService();
    private DispatchProcess dispatchProcess = new DispatchProcess();

    public static ConcurrentHashMap<String,ServerModel> BROKEN_SERVER_MAP = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,RpcServerModel> BROKEN_RPC_SERVER_MAP = new ConcurrentHashMap<>();

    public static DispatcherService getInstance() {
        return INSTANCE;
    }

    private DispatcherService() {
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

    /**
     * 定时检测服务资源汇报状态（客户端每四分钟会汇报一次服务状态到服务器）
     */
    public void init() {
        try {
            SCHDULE_SERVICE.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    checkIMServerStatus();

                    checkRPCServerStatus();
                }
            }, FIVE_MINUTES + ONE_MINUTES, ONE_MINUTES, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("dispatcher scheduleService error:{}", e);
        }
    }

    /**
     * 检测IM 逻辑层和接入层服务器状态
     */
    public void checkIMServerStatus(){
        try {
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            Map<String, String> map = cluster.hgetAll(Global.REDIS_SERVERS_KEY);
            long nowTime = System.currentTimeMillis();
            if (map != null) {
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    ServerModel server = JSON.parseObject(entry.getValue(), ServerModel.class);
                    long lastUpdateTime = server.getUpdateTime();
                    //客户端每3分钟一定会汇报一次服务
                    if (nowTime - lastUpdateTime > (FIVE_MINUTES + ONE_MINUTES)) {
                        String ipPort = entry.getKey();
                        //redis del
                        cluster.hdel(Global.REDIS_SERVERS_KEY, ipPort);
                        //记录坏掉的服务
                        BROKEN_SERVER_MAP.put(ipPort,server);

                        //mongo update status
                        server.setStatus(Global.SERVER_STATUS.NONE.value);
                        SERVER_DAO.saveOrUpdateServer(server);
                        String serverName =  null;
                        if(server.getType() == Global.SERVER_TYPE.GATEWAY.value){
                            serverName = "IM接入服务器";
                        }else if(server.getType() == Global.SERVER_TYPE.LOGIC.value){
                            serverName = "IM逻辑服务器";
                        }else{
                            serverName = server.getType()+"";
                        }
                        sendSMS(ipPort + ":IM服务,状态:异常。原因:未及时汇报服务信息到调度服务,请检查。服务器类型:" + serverName,2);
                        LOG.error("IM服务状况检测 --> 1. 存在服务没有汇报状态,请检查服务状态,服务地址:{},服务类型:{}", ipPort, server.getType());
                    }
                }
                if (set.isEmpty()) {
                    LOG.error("IM服务状况检测 --> 2. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查各服务,定时及时汇报状态!");
                }
                LOG.info("IM服务状况检测 --> 3. 当前调度服务所有服务资源内容:{}", JSON.toJSONString(map));
            } else {
                LOG.error("IM服务状况检测 --> 4. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查......");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("checkIMServerStatus error:{}", e);
        }
    }

    /**
     * 检测RPC服务器状态
     */
    public void checkRPCServerStatus(){
        try {
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            Map<String, String> map = cluster.hgetAll(Global.REDIS_RPC_SERVERS_KEY);
            long nowTime = System.currentTimeMillis();
            if (map != null) {
                Set<Map.Entry<String, String>> set = map.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    RpcServerModel server = JSON.parseObject(entry.getValue(), RpcServerModel.class);
                    long lastUpdateTime = server.getUpdateTime();
                    //客户端每3分钟一定会汇报一次服务
                    if (nowTime - lastUpdateTime > (FIVE_MINUTES + ONE_MINUTES)) {
                        String ipPort = entry.getKey();
                        //redis del
                        cluster.hdel(Global.REDIS_RPC_SERVERS_KEY, ipPort);
                        //记录坏掉的服务
                        BROKEN_RPC_SERVER_MAP.put(ipPort,server);

                        //mongo update status
                        server.setStatus(Global.SERVER_STATUS.NONE.value);
                        SERVER_DAO.saveOrUpdateRPCServer(server);
                        sendSMS(ipPort + ":RPC服务,状态:异常。原因:未及时汇报服务信息到调度服务,请检查。服务器类型:" + server.getType(),2);
                        LOG.error("RPC-IM服务状况检测 --> 1. 存在服务没有汇报状态,请检查服务状态,服务地址:{},服务类型:{}", ipPort, server.getType());
                    }
                }
                if (set.isEmpty()) {
                    LOG.error("RPC-IM服务状况检测 --> 2. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查各服务,定时及时汇报状态!");
                }
                LOG.info("RPC-IM服务状况检测 --> 3. 当前调度服务所有服务资源内容:{}", JSON.toJSONString(map));
            } else {
                LOG.error("RPC-IM服务状况检测 --> 4. 服务资源为空,客户端用户没有汇报状态到调度服务,请检查......");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("checkRPCServerStatus error:{}", e);
        }
    }

    /**
     * 短信息格式必须满足约定规则，刘杨 刘伟 提供规则
     * 内容格式必须满足：  @状态@
     * @param content
     * @param sendCount
     */
    public static void sendSMS(String content,int sendCount){
        if(sendCount <= 0 || sendCount > 3){
            sendCount = 1;
        }
        if(Global.ENV.equals("dev") ){
            return;
        }
        content = content +"。环境:"+Global.ENV;
        for (int i = 0; i < sendCount; i++){
            SMSUtil.SendSMS("【美信报警】", Global.SMS_USERNAME, Global.SMS_PWD, content, Global.MOBILES);
        }
    }

}
