package com.gome.im.dispatcher.model.request;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * 请求拉取匹配的资源服务地址
 * Created by wangshikai on 2016/7/18.
 */
public class ReqServersMsg implements Serializable {
    private int type;  //服务类型
    private Set<Long> cmd;


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
}
