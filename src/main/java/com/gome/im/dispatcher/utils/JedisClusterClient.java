package com.gome.im.dispatcher.utils;

import com.gome.im.dispatcher.global.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by wangshikai on 2016/5/9.
 */
public class JedisClusterClient {
    private static Logger log = LoggerFactory.getLogger(JedisClusterClient.class);

    public static JedisClusterClient INSTANCE = new JedisClusterClient();

    private static JedisCluster jedisCluster = null;
    private static String cluster_ip_ports = "";
    private JedisClusterClient(){
    }
    static {
        if (jedisCluster == null) {
            try {
                Set<HostAndPort> jedisClusterNodes = new HashSet<>();
                Properties properties = PropertiesUtils.LoadProperties(Global.CONFIG_FILE);
                cluster_ip_ports = properties.getProperty("redis_cluster_ip_ports");
                String[] ipArr = cluster_ip_ports.split(",");
                for(String ipPort : ipArr){
                    String[] hostPort = ipPort.split(":");
                    String host = hostPort[0];
                    int port = Integer.parseInt(hostPort[1]);
                    jedisClusterNodes.add(new HostAndPort(host, port));
                }
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxIdle(5);
                config.setMaxTotal(50);
                config.setMinIdle(5);
                config.setMaxWaitMillis(1000 * 10);
                config.setTestOnBorrow(true);
                jedisCluster = new JedisCluster(jedisClusterNodes,config);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }
}
