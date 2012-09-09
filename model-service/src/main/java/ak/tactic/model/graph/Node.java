package ak.tactic.model.graph;

import java.util.HashSet;
import java.util.Set;

import ak.tactic.math.DiscreteProbDensity;
import ak.tactic.math.ModelConfig;
import ak.tactic.math.ParametricDensity;
import ak.tactic.model.deployment.Component;

public class Node {
	Set<Node> dependents = new HashSet<Node>();
	String id;
	DiscreteProbDensity serverResponse = new DiscreteProbDensity(
			ModelConfig.maxTime/ModelConfig.rangeinterval,0,ModelConfig.maxTime,ModelConfig.offset);
	
	
	int vid;
	Component tier;
	boolean mark,edited,transferEdited;
	
	Double x = null;
	Double y = null;
	
	ParametricDensity analysisResponse = null;
	
	NodeModel model = null;
	DiscreteProbDensity modelpdf = null;
	Object modelinput = null;
	Object modeloutput = null;
	int requestCount = 1;
	Object transferFunction = null;
	Double shiftValue = null;
	Subgraph parents = null;
	
	public Node(String name, Component tier, boolean mark) {
		this.id = name;
		this.tier = tier;
		this.mark = mark;
	}
	
	public ParametricDensity getAnalysisResponse() {
		return analysisResponse;
	}
	
	public DiscreteProbDensity getServerResponse() {
		return serverResponse;
	}
	
	public NodeModel getModel() {
		return model;
	}
	
	public void setParents(Subgraph parents) {
		this.parents = parents;
	}
	
	public boolean isMark() {
		return mark;
	}
	
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
	
	public String getId() {
		return id;
	}
	
	public Set<Node> getDependents() {
		return dependents;
	}
	
	public String getName() {
		return id;
	}
}
