import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by wangshikai on 2016/9/7.
 */
public class Test {

    private static Logger log = LoggerFactory.getLogger(Test.class);

    public static String SendMsg(String msg) {
        String receiveMsg = null;
        try {
            if (StringUtils.isEmpty(msg)) {
                log.error("msg:{} is empty!", msg);
                return null;
            }

            InetAddress address = InetAddress.getByName("10.125.72.89");  //服务器地址  10.125.3.61   10.69.16.92
            int port = 8866;  //服务器的端口号
            //创建发送方的数据报信息
            DatagramPacket dataGramPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

            DatagramSocket socket = new DatagramSocket();  //创建套接字
            socket.send(dataGramPacket);  //通过套接字发送数据

            //接收服务器反馈数据
            byte[] buf = new byte[2048];
            DatagramPacket backPacket = new DatagramPacket(buf, buf.length);
            socket.receive(backPacket);  //接收返回数据
            receiveMsg = new String(buf, 0, backPacket.getLength());
            log.info("UDP服务器返回的数据为:" + receiveMsg);
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("error:{}",e);
        }
        return receiveMsg;
    }

    public static void main(String[] args) {
        String msg = Test.SendMsg("{\"requestType\": 2,\"reqServersMsg\": {\"type\": 2}}");
        System.out.println("#############消息内容:"+msg);
    }
}
