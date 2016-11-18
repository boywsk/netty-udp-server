import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangshikai on 2016/10/27.
 */
public class RedisPubsubTest {

    private static final String CHANNEL ="test_pubsub_channel";

    private static void redisSubTest(){
        Jedis jedis = new Jedis("g1rdsc01.im.dev.gomeplus.com",7000);
        jedis.subscribe(new JedisPubSub(){
            public void onMessage(String channel, String message) {
                System.out.println("订阅到内容:"+message);
            }
        },CHANNEL);
    }

    private static void redisPubTest(){
        Jedis jedis = new Jedis("g1rdsc01.im.dev.gomeplus.com",7000);
        jedis.publish(CHANNEL,"发布内容，时间:"+System.currentTimeMillis());
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                redisSubTest();
            }
        });
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i =0 ; i<Integer.MAX_VALUE ;i++){
                    redisPubTest();
                    try {
                        Thread.sleep((i*30)*60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
