package com.gome.im.dispatcher.utils;

import com.gome.im.dispatcher.global.Global;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wangshikai on 2016/7/19.
 */
public class ZKClient {

    private static Logger LOG = LoggerFactory.getLogger(ZKClient.class);

    private static CuratorFramework zkClient;

    private static String ROOT_PATH = Global.ZK_PATH;
    private static String IP_PORT = Global.ZK_IP_PORT;

    private static CountDownLatch latch = new CountDownLatch(1);

    private ZKClient() {
    }

    private static ZKClient INSTANCE = new ZKClient();

    public static ZKClient getInstance() {
        return INSTANCE;
    }

    public CuratorFramework init(int port) {
        zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(IP_PORT)
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                        //.namespace(namespace)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
                .build();
        ConnectionStateListener listener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    LOG.info("ZK连接成功");
                    latch.countDown();
                }
            }
        };
        zkClient.getConnectionStateListenable().addListener(listener);
        zkClient.start();
        try {
            latch.await();

            //创建根节点
            createRootNode(ROOT_PATH);

            //创建子节点
            String childPath = "";
            try {
                InetAddress address = InetAddress.getLocalHost();
                childPath = ROOT_PATH + "/" + address.getHostAddress() + ":" + port;

                createChildNode(childPath);

                LOG.info("创建临时子节点 childPath:{}", childPath);
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                LOG.error("获取本机地址失败,检查......");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return zkClient;
    }

    public static void createRootNode(String rootPath) {
        try {
            Stat state = zkClient.checkExists().forPath(rootPath);
            if (state == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(rootPath);
            } else {
                LOG.info("根节点已经存在,ROOT_PATH:{}", rootPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createChildNode(final String path) {
        try {
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path);
            zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    try {
                        Stat stat = zkClient.checkExists().forPath(path);
                        if(stat == null){
                            LOG.info("ZK 连接状态变化,重新注册临时子节点......");
                            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void getChildrenPath(String rootPath){
        try {
            List<String> childrenPaths = zkClient.getChildren().forPath(rootPath);
            for(String ipPort : childrenPaths){
                System.out.println("--------------------获取到的ZK子节点内容:"+ipPort);
            }
            System.out.println("--------------------获取到的ZK子节点长度:"+childrenPaths.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ZKClient.IP_PORT = "10.125.3.31:2181"; // 开发环境zk地址
        ZKClient.ROOT_PATH = "/im-dispatcher"; // zk 调度服务根节点

        zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(IP_PORT)
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
                .build();
        ConnectionStateListener listener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    LOG.info("ZK连接成功");
                    latch.countDown();
                }
            }
        };
        zkClient.getConnectionStateListenable().addListener(listener);
        zkClient.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getChildrenPath(ZKClient.ROOT_PATH);
    }

}
