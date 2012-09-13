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
		return getNode(modelName, nodeName).getServerResponse();
	}
	
	public Node loadPdf(String modelName, Node node) {
		DistributionData data = dataService.getModelCollection().findOne("{name:#,model:#}", node.getName(), modelName).as(DistributionData.class);
		
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
		
		node.getServerResponse().setRawCount((long)sum).setPdf(histogram);
		return node;		
	}
	
	public Node getNode(String modelName, String nodeName) {
		Node dummyNode = new Node(nodeName, null, false);
		return loadPdf(modelName, dummyNode);
	}
}