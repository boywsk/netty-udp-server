import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

/**
 * Created by wangshikai on 2016/11/9.
 */
public class TestMultiUdp {
    public static void main(String[] args) {
        MultiSend("{\"requestType\": 2,\"reqServersMsg\": {\"type\": 2}}");
    }

    public static void MultiReceive() {
        try {
            //组播服务器地址
            InetAddress address = InetAddress.getByName("224.0.0.5");
            //组播端口
            int port = 6666;
            MulticastSocket server = new MulticastSocket(port);
            server.joinGroup(address);

            //接收服务器反馈数据
            byte[] buf = new byte[2048];
            DatagramPacket backPacket = new DatagramPacket(buf, buf.length);
            while(true){
                server.receive(backPacket);  //接收返回数据
                String receiveMsg = new String(buf, 0, backPacket.getLength());
                System.out.println("UDP服务器返回的数据为:" + receiveMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void MultiSend(String msg) {
        try {
            //组播服务器地址
            InetAddress address = InetAddress.getByName("224.0.0.5");
            int port = 6666;

            MulticastSocket client = new MulticastSocket(port);

            DatagramPacket dataGramPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

            client.send(dataGramPacket);

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
