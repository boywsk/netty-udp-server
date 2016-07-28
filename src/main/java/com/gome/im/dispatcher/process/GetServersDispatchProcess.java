package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqServersMsg;
import com.gome.im.dispatcher.model.response.RspServersMsg;
import com.gome.im.dispatcher.service.DispatcherService;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by wangshikai on 2016/7/27.
 */
public class GetServersDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(GetServersDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public GetServersDispatchProcess(){
        this.requestType = Global.REQUEST_TYPE.GET_RESOURCES.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqServersMsg reqServersMsg = msg.getReqServersMsg();
        int type = reqServersMsg.getType();
        RspServersMsg rsp = null;
        //根据 type 服务类型拉取服务资源
        if (type > 0) {
            rsp = DispatcherService.getInstance().getServersByType(type);
        } else if (type == 0) {
            //根据 cmd 命令字 拉取服务资源
            Set<Long> cmd = reqServersMsg.getCmd();
            if (cmd != null) {
                rsp = DispatcherService.getInstance().getServersByCmd(cmd);
            }
        }
        if (rsp != null) {
            String rspJson = JSON.toJSONString(rsp);
            try {
                ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspJson, CharsetUtil.UTF_8), packet.sender()));
                LOG.info("返回服务器列表成功,服务器列表:{}", rspJson);
            } catch (Exception e) {
                //e.printStackTrace();
                LOG.error("返回服务器列表失败,服务器列表:{}", rspJson);
            }
        } else {
            LOG.info("找不到请求的服务器资源,请求的服务器type:{}", type);
        }
        LOG.info("DispatchProcess ReqServersMsg success");
    }
}
