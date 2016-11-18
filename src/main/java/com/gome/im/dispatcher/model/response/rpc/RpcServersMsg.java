package com.gome.im.dispatcher.model.response.rpc;

import java.io.Serializable;
import java.util.TreeMap;

/**
 *
 * RPC服务器地址
 * Created by wangshikai on 2016/11/10.
 */
public class RpcServersMsg implements Serializable{
    private String cmd;       //命令字类型
    private TreeMap<Double,String> servers;  //服务地址  map<weight,ipPort>

    public TreeMap<Double, String> getServers() {
        return servers;
    }

    public void setServers(TreeMap<Double, String> servers) {
        this.servers = servers;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
