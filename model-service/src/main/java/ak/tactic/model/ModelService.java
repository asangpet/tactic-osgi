package ak.tactic.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ak.tactic.math.MathService;
import ak.tactic.model.template.AnalysisInstance;
import ak.tactic.model.template.ContentWebAnalysis;
import ak.tactic.model.template.DistWebAnalysis;

@Service("modelService")
public class ModelService {
	@Autowired
	ApplicationContext context;
	
	@Autowired
	MathService mathService;
	
	Map<String, AnalysisInstance> instances = new LinkedHashMap<String, AnalysisInstance>();
	
	Map<String, App> appMap = new ConcurrentHashMap<String, App>();
	
	@Autowired
	ContentWebAnalysis contentWeb;
	@Autowired
	DistWebAnalysis distWeb;
	
	@PostConstruct
	public void init() {
		instances.put("cms", contentWeb);
		instances.put("dist", distWeb);
	}
	
	public String buildModel(String collectionName, boolean needRefresh) {
		App app = appMap.get(collectionName);
		if (app == null) {
			app = context.getBean(App.class);
			appMap.put(collectionName, app);
			app.setCollectionName(collectionName);
		}
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, App> existingApps : appMap.entrySet()) {
			sb.append(existingApps.toString()+"\n");
		}
		sb.append("---"+app+"\n");
		sb.append(app.getModel(needRefresh));
		return sb.toString();
	}
	
	public AnalysisInstance getAnalysisInstance(String name) {
		return instances.get(name);
	}
}