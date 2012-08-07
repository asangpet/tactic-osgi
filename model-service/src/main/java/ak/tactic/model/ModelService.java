package ak.tactic.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ak.tactic.data.DataService;
import ak.tactic.data.ResponseInfo;

@Service("modelService")
public class ModelService {
	@Autowired
	private DataService dataService;
	Map<String, Node> nodeMap = new LinkedHashMap<String,Node>();
	
	class Node {
		Set<Node> dependents = new HashSet<Node>();
		String id;
		
		public void setId(String id) {
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			if (id == null) {
				return 37;
			}
			return 37+17*id.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Node)) {
				return false;
			}
			Node nodeObj = (Node)obj;
			if (id == null) {
				return nodeObj.id == null;
			}
			return id.equals(nodeObj.id);
		}
		
		public void addDependent(Node node) {
			dependents.add(node);
		}
		
		@Override
		public String toString() {
			return id;
		}
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
	
	public String buildModel(String collectionName) {
		MongoCollection collection = dataService.getCollection(collectionName);
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
}
