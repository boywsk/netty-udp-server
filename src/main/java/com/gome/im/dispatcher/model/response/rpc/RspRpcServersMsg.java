package com.gome.im.dispatcher.model.response.rpc;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 返回匹配的RPC服务器地址
 * Created by wangshikai on 2016/11/10.
 */
public class RspRpcServersMsg implements Serializable{
    private int type;  //服务类型

    private List<RpcServersMsg> rspServers;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<RpcServersMsg> getRspServers() {
        return rspServers;
    }

    public void setRspServers(List<RpcServersMsg> rspServers) {
        this.rspServers = rspServers;
    }
}
