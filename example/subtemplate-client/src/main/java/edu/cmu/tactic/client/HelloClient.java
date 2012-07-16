package edu.cmu.tactic.client;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.cmu.tactic.service.HelloService;

@Component
public class HelloClient {
	@Autowired HelloService helloService;
	
	Logger log = LoggerFactory.getLogger(HelloClient.class);
	
	@PostConstruct
	public void go() {
		log.info(helloService.sayHello("WOW Client"));
	}
}
