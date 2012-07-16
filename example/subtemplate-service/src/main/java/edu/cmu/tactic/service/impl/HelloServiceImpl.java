package edu.cmu.tactic.service.impl;

import edu.cmu.tactic.service.HelloService;

public class HelloServiceImpl implements HelloService {
	public String sayHello(String name) {
		return "Hello! "+name;
	}
}
