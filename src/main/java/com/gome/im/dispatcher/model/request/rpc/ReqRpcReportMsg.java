package com.gome.im.dispatcher.model.request.rpc;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * RPC服务请求上报服务器资源地址
 * Created by wangshikai on 2016/11/10.
 */
public class ReqRpcReportMsg implements Serializable{
    private int type;  //服务类型
    private Set<String> cmd;  //可以处理的RPC服务请求类型  如 "UserServiceGrpc"
    private String ipPort;  //服务地址如: "127.0.0.1:9000"
    private double weight; //权重   用于服务升级和降级用


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public Set<String> getCmd() {
        return cmd;
    }

    public void setCmd(Set<String> cmd) {
        this.cmd = cmd;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
