package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqReportMsg;
import com.gome.im.dispatcher.service.DispatcherService;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangshikai on 2016/7/27.
 */
public class ReportDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(ReportDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public ReportDispatchProcess(){
        this.requestType = Global.REQUEST_TYPE.REPORT.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqReportMsg reportMsg = msg.getReqReportMsg();
        DispatcherService.getInstance().report(reportMsg.getType(), reportMsg.getCmd(), reportMsg.getIpPort());
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


}
