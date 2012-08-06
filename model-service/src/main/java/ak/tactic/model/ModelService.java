package ak.tactic.model;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;

@Service("modelService")
public class ModelService {
	@Autowired
	private DataService dataService;
	
	public String buildModel(String collection) {
		Iterator<ResponseInfo> iter = dataService.getResponsesByTime(collection).iterator();
		StringBuffer sbuf = new StringBuffer();
		int count = 0;
		double time = 0;
		while (iter.hasNext()) {
			count++;
			ResponseInfo response = iter.next();
			time += response.getResponseTime();
			//sbuf.append(iter.next());
			//sbuf.append("\n");
		}
		sbuf.append("Count:"); sbuf.append(count);
		if (count > 0) {
			sbuf.append("\nAvg Time:"); sbuf.append(time/count);
		}
		return sbuf.toString();
	}
}
