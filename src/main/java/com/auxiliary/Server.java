package com.auxiliary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Server {

	private String ip;
	private int weight;
	
	public Server(String ip) {
		super();
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "Server [ip=" + ip + ", weight=" + weight + "]";
	}	
}
