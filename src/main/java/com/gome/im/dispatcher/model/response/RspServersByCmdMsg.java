package com.gome.im.dispatcher.model.response;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 返回匹配的服务器地址（根据cmd）
 * Created by wangshikai on 2016/7/18.
 */
public class RspServersByCmdMsg implements Serializable{
    private long cmd;       //命令字类型
    private List<String> ipPort;  //服务地址

    public long getCmd() {
        return cmd;
    }

    public void setCmd(long cmd) {
        this.cmd = cmd;
    }

    public List<String> getIpPort() {
        return ipPort;
    }

    public void setIpPort(List<String> ipPort) {
        this.ipPort = ipPort;
    }

}
