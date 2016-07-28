package com.gome.im.dispatcher.model.request;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * 请求上报服务器资源地址
 * Created by wangshikai on 2016/7/18.
 */
public class ReqReportMsg implements Serializable{
    private int type;  //服务类型
    private Set<Long> cmd;  //服务处理命令字类型集合
    private String ipPort;  //服务地址如: "127.0.0.1:9000"


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<Long> getCmd() {
        return cmd;
    }

    public void setCmd(Set<Long> cmd) {
        this.cmd = cmd;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }
}
