package edu.cmu.tactic.osgiclient;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.cmu.tactic.service.HelloService;

@Component
public class WebHelloClient {
	@Autowired HelloService helloService;
	
	Logger log = LoggerFactory.getLogger(WebHelloClient.class);
	
	@PostConstruct
	public void go() {
		log.info(helloService.sayHello("Web bundle context can see this client"));
	}
}