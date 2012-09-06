package ak.tactic.model;

import java.util.HashSet;
import java.util.Set;

public class Node {
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
