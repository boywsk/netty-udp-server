package com.gome.im.dispatcher.model;

import java.util.Set;

/**
 * Created by wangshikai on 2016/11/10.
 */
public class RpcServerModel {
    private int type;        //服务类型
    private Set<String> cmd;   //可以处理的RPC服务请求类型  如 "UserServiceGrpc"
    private String ipPort;   //服务地址如: "127.0.0.1:9000"
    private int status;      //服务状态 0:不可用  1:可用
    private long updateTime; //最近一次服务的汇报更新时间
    private double weight; //权重   用于服务升级和降级用

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<String> getCmd() {
        return cmd;
    }

    public void setCmd(Set<String> cmd) {
        this.cmd = cmd;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
