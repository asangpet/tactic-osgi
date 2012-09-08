package ak.tactic.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;
import ak.tactic.model.data.DistributionData;

@Component
@Scope("prototype")
public class App {
	@Autowired
	private DataService dataService;	
	
	private String collectionName;
	
	Map<String, Node> nodeMap = new LinkedHashMap<String,Node>();
	
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	
	private Node getNode(String id) {
		Node node = nodeMap.get(id);
		if (node == null) {
			node = new Node();
			node.setId(id);
			nodeMap.put(id, node);
		}
		return node;
	}
	
	public String getModel(boolean needRefresh) {
		if (nodeMap.size() == 0 || needRefresh) {
			return buildModel();
		}
		return printModel();
	}
	
	public String printModel() {
		StringBuffer sbuf = new StringBuffer();
		for (Node node : nodeMap.values()) {
			sbuf.append(node.toString());
			sbuf.append(" -> ");
			for (Node dep : node.dependents) {
				sbuf.append(dep);
				sbuf.append(", ");
			}
			sbuf.append("\n");
		}
		return sbuf.toString();
	}
	
	public String buildModel() {
		long computeTime = System.currentTimeMillis();
		MongoCollection collection = dataService.getCollection(collectionName);
		MongoCollection pdfCollection = dataService.getModelCollection();
		
		Iterator<ResponseInfo> iter = collection.find("{}").sort("{timestamp:1}").as(ResponseInfo.class).iterator();
		StringBuffer sbuf = new StringBuffer();
		int count = 0;
		double time = 0;

		while (iter.hasNext()) {
			count++;
			ResponseInfo response = iter.next();
			Node parent = getNode(response.getClient().getAddress()); 
			Node child = getNode(response.getServer().getAddress());
			parent.addDependent(child);
			
			time += response.getResponseTime();
			child.getServerResponse().add(response.getResponseTime());
			if (count < 10) {
				sbuf.append("\n");
				sbuf.append(response);
			}
		}
		sbuf.append("\nCount:"); sbuf.append(count);
		if (count > 0) {
			sbuf.append("\nAvg Time:"); sbuf.append(time/count);
		}
		sbuf.append("\n");
		sbuf.append(printModel());
		
		sbuf.append("\n");
		for (Node node : nodeMap.values()) {
			sbuf.append(node + ":" + node.getServerResponse().getRawCount() + "\n");			
			pdfCollection.save(new DistributionData(node.id, node.getServerResponse()));
		}
		sbuf.append("\n\nTime: ").append(System.currentTimeMillis() - computeTime).append("ms");
		return sbuf.toString();
	}
	
	public DataService getDataService() {
		return dataService;
	}
}