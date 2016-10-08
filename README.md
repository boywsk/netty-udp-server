# netty-udp-server
netty udp server client ，protocol by json

# 本服务为一个资源调度服务。

1.im-dispatcher服务启动时会向zookeeper根路径下注册子节点为服务地址,用户需要到zookeeper的根路径"/im-dispatcher"中
获取im-dispatcher服务地址,获取到的子节点内容如: ["10.69.14.7:8866","10.69.14.8:8866"]。


2.用户向调度服务汇报服务资源和请求服务资源采用UDP通信,JSON协议。

# 服务接口协议说明：

## 客户端需要每五分钟至少向调度服务汇报一次服务资源,否则调度服务检测时会将该资源视为不可用资源。

--------------------------------------------------------类型常量定义--------------------------------------------------------------------------------

一.请求类型
public static enum REQUEST_TYPE {

	REPORT(1),          // 汇报服务资源
	GET_RESOURCES(2);   // 获取服务资源

	public int value;
	private REQUEST_TYPE(int value) {
		this.value = value;
	}
}


二.服务类型：
public static enum SERVER_TYPE {

	GATEWAY(1), // 接入
	LOGIC(2),   // 逻辑
	API(3),     // api
	FILE(4),    // 文件
	ALL(5);     //全部

	public int value;
	private SERVER_TYPE(int value) {
		this.value = value;
	}
}

---------------------------------------------------------------------------------------------------------------------------------------------------------


1.im-dispatcher服务启动时会向zookeeper根路径下注册子节点为服务地址,用户需要到zookeeper的根路径"/im-dispatcher"中
获取im-dispatcher服务地址,获取到的子节点内容如: ["10.69.14.7:8866","10.69.14.8:8866"]。


2.用户向调度服务汇报服务资源和请求服务资源采用UDP通信,JSON协议。

服务接口协议说明：

2.1 用户汇报服务资源消息协议内容示例(JSON):

2.1.1 汇报服务资源请求消息：
{
    "requestType": 1,         //请求类型  1:汇报服务资源
    "reqReportMsg": {         
        "type": 1,                    //服务类型(参考上面客户端服务类型)
        "ipPort": "127.0.0.1:88770",  //服务ipPort (必填)
        "cmd": [                      //当前服务可以处理的命令字集合
            123,
            456
        ]
    }
}

2.1.2 汇报资源时服务端返回消息内容(客户端可以不处理,根据情况):
{
    "type": 1,
    "ipPort": "127.0.0.1:88770",
    "cmd": [
        123,
        456
    ]
}

------------------------------------------------------------------------------------------------------------------------------------------------------------------------


2.2 用户获取服务资源消息协议内容示例(JSON):

2.2.1 用户获取服务资源请求消息(根据服务器类型  type)：
{
    "requestType": 2,       //请求类型  2:获取服务资源
    "reqServersMsg": {
        "type": 2           //请求获取的服务资源类型(参考上面客户端服务类型)
    }
}

2.2.2 服务器返回服务资源消息(根据服务器类型  type)：
{
    "type": 2,
    "rspServers": [
        {
            "cmd": 456, 
            "ipPort": [
                "10.125.3.11:8001"
            ]
        }, 
        {
            "cmd": 522, 
            "ipPort": [
                "10.125.3.11:8001"
            ]
        }, 
        {
            "cmd": 524, 
            "ipPort": [
                "10.125.3.11:8001"
            ]
        }, 
        {
            "cmd": 123, 
            "ipPort": [
                "10.125.3.11:8001"
            ]
        }
    ]
}

------------------------------------------------------------------------------------------------------------------------------------------------------------------------


2.2.3 用户获取服务资源请求消息(根据命令字  cmd)：

{
	"requestType": 2,
    "reqServersMsg": {
        "cmd": [
            456, 
            123
        ]
    }
}


2.2.4 服务器返回服务资源消息(根据命令字  cmd)：

{
    "rspServers": [
        {
            "cmd": 456, 
            "ipPort": [
                "127.0.0.1:88770", 
                "127.0.0.1:88772", 
                "127.0.0.1:88771"
            ]
        }, 
        {
            "cmd": 123, 
            "ipPort": [
                "127.0.0.1:88770", 
                "127.0.0.1:88772", 
                "127.0.0.1:88771"
            ]
        }
    ]
}
