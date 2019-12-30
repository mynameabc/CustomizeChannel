# CustomizeChannel
新通道逻辑:
四方下单调起第三方通道
再轮询选出账号, 封装json数据发送到前线, 通道收到前线返回的URL, 转发给调用方(四方)

通知成功接口

四方传过来的参数:

1.平台订单号
2.支付方式
3.金额

三方传前线参数:
1.账号
2.金额

http://localhost:8890/channel/pay?platformOrderNo=1&payType=3&orderAmount=80&channel=GuoMei&notifyUrl=127.0.0.1

command
0:心跳
1:登陆
2:登陆失败
3:登陆成功
4.下单


----------------------------

1.轮询账号
2.讨论还有哪些问题
3.
4.下单小号不再做为websocket客户端连接账号, 改为在客户端随机生成字符
	4.1:一开始客户端开启30个连接
	4.2:第一个小号下单完成, 客户端返回支付联接, 当前下单小号当天数量加1

------------------------------------------------------
小号下满通知websocket再登陆(换账号)

1.建立连接时把可用的下单小号分配给WebSocket客户端
2.等待客户端返回消息变更客户端连接账号的状态(更新到数据库<登陆失败:0, 登陆成功:1>), 登陆失败需要再次给予一个可用小号(先查是否有可用的下单小号), 成功则变更数据库状态
3.下单时轮询选取出可用下单小号

@Data
public class Client {
	
	private String placeOrderName;
	private String placeOrderPassword;
	private String clientUserName;
	private WebSocket webSocket;
}


public class DistributeIdentity {
	

}

job每天晚上23:59分跑, 把client_user表的number字段值设成0

先查找出 (status.equls("0") && number < 20) 的记录

第一层:webSocket账号
第二层:下单小号

下单时判断是否有可链接的webSocket账号(没有可用联接<webSocket账号>)
再判断是否有可下单的小号(下单账号不足)
-----------------------------------------
webSocket登陆时链接 下单小号传过去时把表更新下websocket链接与下单小号的关系

clientUserMapper

无可用下单小号时无处理



job
	1.12:00后关掉下单开关, 把client_user表的number字段全设成0
	2.收货

四方
账号选举
判断下单小号
前线和四方的加签与验签

----------------------------------------------------------------------------------------------------------------------------------------

服务器信息

四方测试服务器
47.75.188.136
root
Yizhifu418

三方测试服务器
47.112.167.170
root
Yizhifu419
