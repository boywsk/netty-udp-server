/**
 * Created by wangshikai on 2016/9/23.
 */
public class Test123 {
    public static void main(String[] args) {
        String appId = "gomeplus_pre";
        System.out.println(appId.getBytes().length);
        byte[] buf = new byte[32];
        System.arraycopy(appId.getBytes(),0,buf,0,appId.getBytes().length);
        System.out.println(new String(buf));
    }
}
