package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.RpcServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.rpc.ReqRpcServersMsg;
import com.gome.im.dispatcher.model.response.rpc.RpcServersMsg;
import com.gome.im.dispatcher.model.response.rpc.RspRpcServersMsg;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * 拉取RPC服务器资源
 * Created by wangshikai on 2016/11/14.
 */
public class RPCServersGetDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(RPCServersGetDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public RPCServersGetDispatchProcess() {
        this.requestType = Global.REQUEST_TYPE.RPC_PULL.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqRpcServersMsg reqServersMsg = msg.getReqRpcServersMsg();
        int type = reqServersMsg.getType();
        RspRpcServersMsg rsp = null;
        //根据 type 服务类型拉取服务资源
        try {
            //请求指定类型的服务资源
            rsp = getServersByType(type);
        } catch (Exception e) {
            LOG.error("error:{}", e);
        }
        String rspJson = JSON.toJSONString(rsp);
        try {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspJson, CharsetUtil.UTF_8), packet.sender()));
            LOG.info("返回服务器列表成功,请求服务类型:{},服务器列表:{}", type, rspJson);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("返回服务器列表失败,请求服务类型:{},服务器列表:{}", type, rspJson);
        }
        LOG.info("DispatchProcess ReqServersMsg success");
    }

    /**
     * 客户端拉取服务器列表
     *
     * @param type
     * @return
     */
    public static RspRpcServersMsg getServersByType(int type) {
        RspRpcServersMsg rsp = null;
        JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
        Map<String, String> map = cluster.hgetAll(Global.REDIS_RPC_SERVERS_KEY);
        if (map != null) {
            rsp = new RspRpcServersMsg();
            Map<String, HashMap<String, Double>> cmdsMap = new HashMap<>();
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
                RpcServerModel server = JSON.parseObject(entry.getValue(), RpcServerModel.class);
                if (server.getType() == type) {
                    Set<String> cmds = server.getCmd();
                    for (String cmd : cmds) {
                        String ipPort = entry.getKey();
                        HashMap<String, Double> ipWeightMap = cmdsMap.get(cmd);
                        if (ipWeightMap == null) {
                            ipWeightMap = new HashMap<>();
                            ipWeightMap.put(ipPort, server.getWeight());
                            cmdsMap.put(cmd, ipWeightMap);
                        } else {
                            ipWeightMap.put(ipPort, server.getWeight());
                        }
                    }
                }
            }
            if (!cmdsMap.isEmpty()) {
                List<RpcServersMsg> rspRpcServersMsg = new ArrayList<>();
                Set<Map.Entry<String, HashMap<String, Double>>> cmdsMapSet = cmdsMap.entrySet();
                for (Map.Entry<String, HashMap<String, Double>> en : cmdsMapSet) {
                    RpcServersMsg rpcServersMsg = new RpcServersMsg();
                    rpcServersMsg.setCmd(en.getKey());
                    TreeMap<Double, String> weightMap = new TreeMap<>();
                    caculWeight(en.getValue(), weightMap);
                    rpcServersMsg.setServers(weightMap);
                    rspRpcServersMsg.add(rpcServersMsg);
                }
                rsp.setType(type);
                rsp.setRspServers(rspRpcServersMsg);
            }
            LOG.info("getServersByType success,type:{}", type);
        }
        return rsp;
    }

    /**
     * 计算服务权重 ：划分权重区间
     * @param ipPortWeightMap
     * @param treeMap
     */
    public static void caculWeight(HashMap<String, Double> ipPortWeightMap, TreeMap<Double, String> treeMap) {
        Set<String> set = ipPortWeightMap.keySet();
        for (String ipPort : set) {
            double weight = ipPortWeightMap.get(ipPort);
            if(weight == 0){
                //服务器权重为0的情况,停止分发
                continue;
            }
            double totalWeight = treeMap.size() == 0 ? 0 : treeMap.lastKey().doubleValue();
            treeMap.put(weight + totalWeight, ipPort);
        }
    }

}
