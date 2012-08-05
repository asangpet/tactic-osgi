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
	
	public String buildModel() {
		Iterator<ResponseInfo> iter = dataService.getResponses().iterator();
		StringBuffer sbuf = new StringBuffer();
		while (iter.hasNext()) {
			sbuf.append(iter.next());
			sbuf.append("\n");
		}
		return sbuf.toString();
	}
}
