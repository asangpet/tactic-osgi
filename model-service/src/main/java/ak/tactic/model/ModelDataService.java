package ak.tactic.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ak.tactic.data.DataService;
import ak.tactic.model.data.DistributionData;
import ak.tactic.model.graph.Node;
import ak.tactic.model.math.DiscreteProbDensity;

@Service("modelDataService")
public class ModelDataService {
	@Autowired
	DataService dataService;
	
	public DiscreteProbDensity getPdf(String modelName, String nodeName) {
		DistributionData data = dataService.getModelCollection().findOne("{name:#,model:#}", nodeName, modelName).as(DistributionData.class);
		Node dummyNode = new Node(nodeName, null, false);
		
		double sum = 0;
		double[] histogram = data.getData();
		for (double value : histogram) {
			sum += value;
		}
		if (sum > 0) {
			for (int i=0;i<histogram.length;i++) {
				histogram[i] = histogram[i] / sum;
			}
		}
		
		dummyNode.getServerResponse().setRawCount((long)sum).setPdf(histogram);
		return dummyNode.getServerResponse();
	}
}