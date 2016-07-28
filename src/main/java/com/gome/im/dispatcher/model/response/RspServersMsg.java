package com.gome.im.dispatcher.model.response;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 返回匹配的服务器地址
 * Created by wangshikai on 2016/7/18.
 */
public class RspServersMsg implements Serializable{
    private int type;  //服务类型
    private List<String> ipPort;  //服务地址

    private List<RspServersByCmdMsg> rspServers;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getIpPort() {
        return ipPort;
    }

    public void setIpPort(List<String> ipPort) {
        this.ipPort = ipPort;
    }

    public List<RspServersByCmdMsg> getRspServers() {
        return rspServers;
    }

    public void setRspServers(List<RspServersByCmdMsg> rspServers) {
        this.rspServers = rspServers;
    }
}
