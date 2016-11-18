package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.ServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqServersMsg;
import com.gome.im.dispatcher.model.response.RspServersByCmdMsg;
import com.gome.im.dispatcher.model.response.RspServersMsg;
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
 * IM服务拉取服务器资源(逻辑服务和介入层服务)
 * Created by wangshikai on 2016/7/27.
 */
public class GetServersDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(GetServersDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public GetServersDispatchProcess() {
        this.requestType = Global.REQUEST_TYPE.GET_RESOURCES.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqServersMsg reqServersMsg = msg.getReqServersMsg();
        int type = reqServersMsg.getType();
        RspServersMsg rsp = null;
        //根据 type 服务类型拉取服务资源
        try {
            if (type > 0) {
                if (type == Global.SERVER_TYPE.ALL.value) { //请求全部服务
                    List<RspServersMsg> rspAll = new ArrayList<>();
                    for (Global.SERVER_TYPE serverType : Global.SERVER_TYPE.values()) {
                        if (serverType.value == Global.SERVER_TYPE.ALL.value) {
                            continue;
                        }
                        type = serverType.value;
                        RspServersMsg rspServersMsg = getServersByType(type);
                        if (rspServersMsg.getType() != 0) {
                            rspAll.add(rspServersMsg);
                        }
                    }
                    String rspJson = JSON.toJSONString(rspAll);
                    try {
                        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspJson, CharsetUtil.UTF_8), packet.sender()));
                        LOG.info("返回服务器列表成功,服务器列表:{}", rspJson);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOG.error("返回服务器列表失败,服务器列表:{}", rspJson);
                    }
                    return;
                } else {   //请求指定类型的服务资源
                    rsp = getServersByType(type);
                }
            } else if (type == 0) {
                //根据 cmd 命令字 拉取服务资源
                Set<Long> cmd = reqServersMsg.getCmd();
                if (cmd != null) {
                    rsp = getServersByCmd(cmd);
                }
            }
        } catch (Exception e) {
            LOG.error("error:{}", e);
        }
        String rspJson = JSON.toJSONString(rsp);
        try {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspJson, CharsetUtil.UTF_8), packet.sender()));
            LOG.info("返回服务器列表成功,服务器列表:{}", rspJson);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("返回服务器列表失败,服务器列表:{}", rspJson);
        }
        LOG.info("DispatchProcess ReqServersMsg success");
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
            Map<Long, List<String>> cmdsMap = new HashMap<>();
            Set<Map.Entry<String, String>> set = map.entrySet();
            for (Map.Entry<String, String> entry : set) {
                ServerModel server = JSON.parseObject(entry.getValue(), ServerModel.class);
                if (server.getType() == type) {
                    Set<Long> cmds = server.getCmd();
                    for (Long cmd : cmds) {
                        String ipPort = entry.getKey();
                        List<String> ipList = cmdsMap.get(cmd);
                        if (ipList == null) {
                            ipList = new ArrayList<>();
                            ipList.add(ipPort);
                            cmdsMap.put(cmd, ipList);
                        } else {
                            ipList.add(ipPort);
                        }
                    }
                }
            }
            if (!cmdsMap.isEmpty()) {
                List<RspServersByCmdMsg> rspServersByCmdMsgList = new ArrayList<>();
                Set<Map.Entry<Long, List<String>>> cmdsMapSet = cmdsMap.entrySet();
                for (Map.Entry<Long, List<String>> en : cmdsMapSet) {
                    RspServersByCmdMsg rspServersByCmdMsg = new RspServersByCmdMsg();
                    rspServersByCmdMsg.setCmd(en.getKey());
                    rspServersByCmdMsg.setIpPort(en.getValue());
                    rspServersByCmdMsgList.add(rspServersByCmdMsg);
                }
                rsp.setType(type);
                rsp.setRspServers(rspServersByCmdMsgList);
            }
            LOG.info("getServersByType success,type:{}", type);
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
}
