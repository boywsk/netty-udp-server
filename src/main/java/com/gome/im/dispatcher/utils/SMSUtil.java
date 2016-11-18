package com.gome.im.dispatcher.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by wangshikai on 2016/11/7.
 */
public class SMSUtil {

    private static Logger LOG = LoggerFactory.getLogger(SMSUtil.class);

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //发送内容
        String content = "127.0.0.1:9999,状态:异常";
        String sign = "【美信报警】";  //规则签名

        // 创建StringBuffer对象用来操作字符串
        StringBuffer sb = new StringBuffer("http://web.cr6868.com/asmx/smsservice.aspx?");

        // 向StringBuffer追加用户名
        sb.append("name=15313729295");

        // 向StringBuffer追加密码（登陆网页版，在管理中心--基本资料--接口密码，是28位的）
        sb.append("&pwd=3040D5ACE3A9C2EE25C46F42FB9C");

        // 向StringBuffer追加手机号码
        sb.append("&mobile=15001338950,15652317963,15201684568");

        // 向StringBuffer追加消息内容转URL标准码
        sb.append("&content=" + URLEncoder.encode(content, "UTF-8"));

        //追加发送时间，可为空，为空为及时发送
        sb.append("&stime=");

        //加签名
        sb.append("&sign=" + URLEncoder.encode(sign, "UTF-8"));

        //type为固定值pt  extno为扩展码，必须为数字 可为空
        sb.append("&type=pt&extno=");
        // 创建url对象
        //String temp = new String(sb.toString().getBytes("GBK"),"UTF-8");
        System.out.println("sb:" + sb.toString());
        URL url = new URL(sb.toString());

        // 打开url连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置url请求方式 ‘get’ 或者 ‘post’
        connection.setRequestMethod("POST");

        // 发送
        InputStream is = url.openStream();

        //转换返回值
        String returnStr = SMSUtil.convertStreamToString(is);

        // 返回结果为‘0，20140009090990,1，提交成功’ 发送成功   具体见说明文档
        LOG.info("返回结果:" + returnStr);
        // 返回发送结果


    }

    /**
     * 转换返回值类型为UTF-8格式.
     *
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is) {
        StringBuilder sb1 = new StringBuilder();
        byte[] bytes = new byte[4096];
        int size = 0;

        try {
            while ((size = is.read(bytes)) > 0) {
                String str = new String(bytes, 0, size, "UTF-8");
                sb1.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb1.toString();
    }


    /**
     *      短信规则：   签名主题: 【美信报警】    内容: @状态@
     *
     * 发送短信息
     * @param sign    短信签名主题
     * @param username 短信平台用户名
     * @param pwd       短信平台用户密码
     * @param content 短信信息具体内容
     * @param mobiles 多个手机号 以","分割
     * @return
     */
    public static String SendSMS(String sign,String username,String pwd,String content, String mobiles) {
        String returnStr = null;
        try {
            // 创建StringBuffer对象用来操作字符串
            StringBuffer sb = new StringBuffer("http://web.cr6868.com/asmx/smsservice.aspx?");

            // 向StringBuffer追加用户名
            sb.append("name="+ username);

            // 向StringBuffer追加密码（登陆网页版，在管理中心--基本资料--接口密码，是28位的）
            sb.append("&pwd="+ pwd);

            // 向StringBuffer追加手机号码
            sb.append("&mobile="+mobiles);

            // 向StringBuffer追加消息内容转URL标准码
            sb.append("&content=" + URLEncoder.encode(content, "UTF-8"));

            //追加发送时间，可为空，为空为及时发送
            sb.append("&stime=");

            //加签名
            sb.append("&sign=" + URLEncoder.encode(sign, "UTF-8"));

            //type为固定值pt  extno为扩展码，必须为数字 可为空
            sb.append("&type=pt&extno=");
            // 创建url对象
            //String temp = new String(sb.toString().getBytes("GBK"),"UTF-8");
            System.out.println("sb:" + sb.toString());
            URL url = new URL(sb.toString());

            // 打开url连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置url请求方式 ‘get’ 或者 ‘post’
            connection.setRequestMethod("POST");

            // 发送
            InputStream is = url.openStream();

            //转换返回值
            returnStr = SMSUtil.convertStreamToString(is);

            // 返回结果为‘0，20140009090990,1，提交成功’ 发送成功   具体见说明文档
            LOG.info("返回结果:" + returnStr);
            // 返回发送结果
        } catch (IOException e) {
            //e.printStackTrace();
            LOG.error("error:{}", e);
        }
        return returnStr;
    }
}
