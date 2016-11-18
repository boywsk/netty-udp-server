package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.RpcServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.rpc.ReqRpcReportMsg;
import com.gome.im.dispatcher.service.DispatcherService;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.Set;

/**
 * RPC服务汇报服务状态
 * Created by wangshikai on 2016/11/14.
 */
public class RPCReportDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(RPCReportDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public RPCReportDispatchProcess() {
        this.requestType = Global.REQUEST_TYPE.RPC_REPORT.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqRpcReportMsg reportMsg = msg.getReqRpcReportMsg();
        report(reportMsg.getType(), reportMsg.getCmd(), reportMsg.getIpPort(), reportMsg.getWeight());
        String rspReportJson = JSON.toJSONString(reportMsg);
        try {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspReportJson, CharsetUtil.UTF_8), packet.sender()));
            LOG.info("服务端返回数据信息:{}", rspReportJson);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("服务端返回数据信息失败:{}", rspReportJson);
        }
        LOG.info("DispatchProcess ReqReportMsg success");
    }


    public static void report(int type, Set<String> cmds, String ipPort, double weight) {
        if(type != Global.SERVER_TYPE.RPC.value){
            LOG.error("错误数据:客户端汇报资源的服务类型和请求类型不匹配，" +
                    "服务地址:{},请求类型为RPC服务汇报类型:{}，" +
                    "汇报的服务类型应该为:{}," +
                    "实际汇报类型:{}", ipPort,Global.REQUEST_TYPE.RPC_REPORT.value,Global.SERVER_TYPE.RPC.value,type);
            return;
        }
        if (StringUtils.isEmpty(ipPort)) {
            LOG.error("错误数据:客户端汇报资源的ipPort值为空:{}", ipPort);
            return;
        }
        if (DispatcherService.BROKEN_RPC_SERVER_MAP.containsKey(ipPort)) {
            DispatcherService.sendSMS(ipPort+":RPC服务,状态:OK", 1);//发信息服务收到汇报
            DispatcherService.BROKEN_RPC_SERVER_MAP.remove(ipPort);
        }
        long nowTime = System.currentTimeMillis();
        RpcServerModel server = new RpcServerModel();
        server.setStatus(Global.SERVER_STATUS.OK.value);
        server.setType(type);
        server.setCmd(cmds);
        server.setIpPort(ipPort);
        server.setWeight(weight);
        server.setUpdateTime(nowTime);
        try {
            //存入redis
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            cluster.hset(Global.REDIS_RPC_SERVERS_KEY, server.getIpPort(), JSON.toJSONString(server));
            //存入mongodb
            DispatcherService.SERVER_DAO.saveOrUpdateRPCServer(server);
            LOG.info("report success,ipPort:{},type:{},weight:{}", server.getIpPort(), server.getType(), server.getWeight());
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("dispatcher report error:{},type:{},ipPort:{}", e, server.getType(), server.getIpPort());
        }
    }


}
