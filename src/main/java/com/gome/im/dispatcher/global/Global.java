package com.gome.im.dispatcher.global;


import com.gome.im.dispatcher.utils.PropertiesUtils;

import java.util.Properties;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class Global {
    public static String CONFIG_FILE = "config.properties";

    /**
     * redis 中存储的服务器
     */
    public static final String REDIS_SERVERS_KEY = "dispatcher_servers_key";

    //zookeeper
    public static String ZK_IP_PORT;
    public static String ZK_PATH;

    //mongo dbName
    public static String MONGODB_DBNAME;

    static {
        Properties properties = PropertiesUtils.LoadProperties(CONFIG_FILE);
        ZK_IP_PORT = properties.getProperty("zookeeperAddress");
        ZK_PATH = properties.getProperty("zookeeperPath");
        MONGODB_DBNAME = properties.getProperty("mongodb.dbName");
    }

    /**
     * 服务器状态类型
     */
    public static enum SERVER_STATUS {
        NONE(0),
        OK(1);
        public int value;
        private SERVER_STATUS(int value){
            this.value = value;
        }
    }

    /**
     * 请求类型
     */
    public static enum REQUEST_TYPE {

        REPORT(1),          // 汇报服务资源
        GET_RESOURCES(2);   // 获取服务资源

        public int value;
        private REQUEST_TYPE(int value) {
            this.value = value;
        }
    }


    /**
     * 汇报的服务器类型
     */
    public static enum SERVER_TYPE {

        GATEWAY(1), // 接入
        LOGIC(2), // 逻辑
        API(3), // api
        FILE(4); // 文件

        public int value;

        private SERVER_TYPE(int value) {
            this.value = value;
        }
    }

}
