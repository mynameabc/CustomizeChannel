package com.pojo.customize;

import com.websokcet.WebSocket;
import lombok.Data;

@Data
public class Client {

	/**
	 * 下单小号账号
	 */
	private String placeOrderName;

	/**
	 * 下单小号密码
	 */
	private String placeOrderPassword;

	/**
	 * 前线登陆状态(0:未登陆, 1:登陆)
	 */
	private int placeOrderLoginStatus;

	/**
	 * 客户端连接标识
	 */
	private String clientUserName;

	/**
	 * webSocket对象
	 */
	private WebSocket webSocket;
}
