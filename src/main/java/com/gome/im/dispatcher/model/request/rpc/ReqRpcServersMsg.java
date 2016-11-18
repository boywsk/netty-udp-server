package com.gome.im.dispatcher.model.request.rpc;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * RPC服务请求拉取服务器资源地址
 * Created by wangshikai on 2016/11/10.
 */
public class ReqRpcServersMsg implements Serializable {
    private int type;  //服务类型
    private Set<String> cmd;


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
}
