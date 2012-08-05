package ak.tactic.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;
import ak.tactic.model.ModelService;

@Controller
public class IndexController {
	Logger log = LoggerFactory.getLogger(IndexController.class);
	
	@Autowired
	private DataService dataService;
	
	@Autowired
	private ModelService modelService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model) {
		//log.info(helloService.sayHello("Hello world"));
		String result = "";
		for (ResponseInfo info : dataService.getResponses()) {
			result += info.toString();
		}
		model.addAttribute("helloLabel", result);
		//model.addAttribute("helloLabel", "Not much here, move along");
		return "index";
	}
	
	@RequestMapping(value = "/model", method = RequestMethod.GET)
	public String testModel(Model model) {
		model.addAttribute("helloLabel", modelService.buildModel());
		return "index";
	}
	
	@RequestMapping(value = "/play", method = RequestMethod.GET)
	public String play(Model model) {
		return "play";
	}
}
