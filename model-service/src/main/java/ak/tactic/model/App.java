package ak.tactic.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;
import ak.tactic.model.data.CoarrivalData;
import ak.tactic.model.data.DistributionData;
import ak.tactic.model.graph.Node;
import ak.tactic.model.math.ResponseAnalysis;

@Component
@Scope("prototype")
public class App {
	private static Logger log = LoggerFactory.getLogger(App.class); 
	
	@Autowired
	private DataService dataService;	
	
	@Autowired
	private ModelDataService modelData;
	
	private String collectionName;
	
	Map<String, Node> nodeMap = new LinkedHashMap<String,Node>();
	
	Map<String, Double> coarrivalMatrix = new HashMap<String,Double>();
	
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	
	private Node getNode(String id) {
		Node node = nodeMap.get(id);
		if (node == null) {
			node = new Node(id, null, false);
			node.setId(id);
			nodeMap.put(id, node);
		}
		return node;
	}
	
	public String getModel(boolean needRefresh) {
		if (needRefresh) {
			return buildModel();
		} else {
			return loadModel();
			/*
			if (nodeMap.size() == 0) {
				return loadModel();
			} else {
				return printModel();
			}*/
		}
	}
	
	public String printModel() {
		StringBuffer sbuf = new StringBuffer();
		String[] order = new String[nodeMap.size()];
		int pos = 0;
		for (Node node : nodeMap.values()) {
			sbuf.append(node.toString());
			sbuf.append(" -> ");
			for (Node dep : node.getDependents()) {
				sbuf.append(dep);
				sbuf.append(", ");
			}
			sbuf.append("\n");
			order[pos++] = node.getName();
		}
		Arrays.sort(order);
		
		sbuf.append(String.format("%16s",""));
		for (String rNode : order) {
			sbuf.append(String.format("%16s",rNode));
		}
		sbuf.append("\n");
		for (String rNode: order) {
			sbuf.append(String.format("%16s",rNode));
			for (String cNode: order) {
				Double value = coarrivalMatrix.get(rNode+"|"+cNode);
				sbuf.append(String.format("%16.5f", value));
				
			}
			sbuf.append("\n");
		}
		
		/*
		for (Map.Entry<String, Double> codata : coarrivalMatrix.entrySet()) {
			sbuf.append(codata.getKey()+" ");
			sbuf.append(String.format("%8.3f", codata.getValue()));
			sbuf.append("\n");
		}
		*/
		return sbuf.toString();
	}
	
	void buildCoarrivalMatrix() {
		MongoCollection coCollection = dataService.getCollection("coarrival");
		coCollection.remove("{m:#}",collectionName);
		for (Map.Entry<String, Node> entry1 : nodeMap.entrySet()) {
			for (Map.Entry<String, Node> entry2 : nodeMap.entrySet()) {
				log.info("Calculating pair {},{}", entry1.getKey(), entry2.getKey());
				double coarrival = ResponseAnalysis.findCoarrivalProb(getDataIterator(entry1.getKey()), getDataIterator(entry2.getKey()));
				CoarrivalData coData = new CoarrivalData(collectionName, entry1.getKey(), entry2.getKey(), coarrival);
				coCollection.save(coData);
			}			
		}
	}
	
	Iterator<ResponseInfo> getDataIterator(String name) {
		MongoCollection collection = dataService.getCollection(collectionName);
		Iterator<ResponseInfo> iter = collection.find("{'server.address':#}", name).as(ResponseInfo.class).iterator();
		return iter;
	}
	
	public String loadModel() {
		String modelName = collectionName;
		MongoCollection pdfCollection = dataService.getModelCollection();
		boolean found = false;
		for (DistributionData distData : pdfCollection.find("{model:#}",modelName).as(DistributionData.class)) {
			found = true;
			modelData.loadPdf(modelName,getNode(distData.getName()));
		}
		
		MongoCollection coCollection = dataService.getCoarrivalCollection();
		for (CoarrivalData coData : coCollection.find("{m:#}",modelName).as(CoarrivalData.class)) {
			found = true;
			String coKey = coData.getReferencedComponent()+"|"+coData.getInterferingComponent();
			coarrivalMatrix.put(coKey, coData.getCoarrival());
		}
		
		if (!found) {
			return buildModel();
		}
		return printModel();
	}
	
	public String buildModel() {
		long computeTime = System.currentTimeMillis();
		MongoCollection collection = dataService.getCollection(collectionName);
		MongoCollection pdfCollection = dataService.getModelCollection();
		collection.ensureIndex("{timestamp:1}");
		
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
		pdfCollection.remove("{model:#}", collectionName);
		for (Node node : nodeMap.values()) {
			sbuf.append(node + ":" + node.getServerResponse().getRawCount() + "\n");			
			pdfCollection.save(new DistributionData(collectionName, node.getId(), node.getServerResponse()));
		}
		
		// build co-arrival matrix
		buildCoarrivalMatrix();
		
		sbuf.append("\n\nTime: ").append(System.currentTimeMillis() - computeTime).append("ms");
		return sbuf.toString();
	}
	
	public DataService getDataService() {
		return dataService;
	}
	
	public Map<String, Double> getCoarrivalMatrix() {
		return coarrivalMatrix;
	}
}