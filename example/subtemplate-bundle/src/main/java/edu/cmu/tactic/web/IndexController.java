package edu.cmu.tactic.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.cmu.tactic.service.HelloService;

@Controller
public class IndexController {
	Logger log = LoggerFactory.getLogger(IndexController.class);
	
	@Autowired
	private HelloService helloService;
	
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String index(Model model) {
		//log.info(helloService.sayHello("Hello world"));
		model.addAttribute("helloLabel", helloService.sayHello("Ak"));
		//model.addAttribute("helloLabel", "Not much here, move along");
		return "index";
	}
	
	@RequestMapping(value = "/play", method = RequestMethod.GET)
	public String play(Model model) {
		return "play";
	}
}
