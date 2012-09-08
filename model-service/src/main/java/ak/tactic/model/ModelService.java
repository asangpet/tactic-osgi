package ak.tactic.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ak.tactic.math.MathService;

@Service("modelService")
public class ModelService {
	@Autowired
	ApplicationContext context;
	
	@Autowired
	MathService mathService;
	
	Map<String, App> appMap = new ConcurrentHashMap<String, App>();
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
}