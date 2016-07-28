package com.gome.im.dispatcher.model;

import java.util.Set;

/**
 *
 * 服务器资源实体
 * Created by wangshikai on 2016/7/18.
 */
public class ServerModel {
    private int type;        //服务类型
    private Set<Long> cmd;   //服务可以处理的命令字类型集合
    private String ipPort;   //服务地址如: "127.0.0.1:9000"
    private int status;      //服务状态 0:不可用  1:可用
    private long updateTime; //最近一次服务的汇报更新时间

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

}
