package ak.tactic.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;
import ak.tactic.model.ModelService;
import ak.tactic.model.template.AnalysisInstance;

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
		ResponseInfo info = dataService.getCollection("responseTime").findOne().as(ResponseInfo.class);
		if (info != null) {
			result += info.toString();
		}
		model.addAttribute("helloLabel", result);
		//model.addAttribute("helloLabel", "Not much here, move along");
		return "index";
	}
	
	@RequestMapping(value = "/model/{collection}", method = RequestMethod.GET)
	public String testModel(@PathVariable String collection, Model model) {
		model.addAttribute("helloLabel", modelService.buildModel(collection, false));
		return "index";
	}
	
	@RequestMapping(value = "/model/{collection}/refresh", method = RequestMethod.GET)
	public String refreshModel(@PathVariable String collection, Model model) {
		model.addAttribute("helloLabel", modelService.buildModel(collection, true));
		return "index";
	}
	
	@RequestMapping(value = "/analysis", method = RequestMethod.GET)
	public String analysisModel(Model model) {
		AnalysisInstance instance = modelService.getAnalysisInstance("dist");
		Map<String, double[]> map = instance.analyze();
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, double[]> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" = [");
			for (double v : entry.getValue()) {
				sb.append(v);
				sb.append(",");
			}
			sb.append("];\n");
		}
		model.addAttribute("helloLabel", sb.toString());
		return "index";
	}
	
	@RequestMapping(value = "/play", method = RequestMethod.GET)
	public String play(Model model) {
		return "play";
	}
}
