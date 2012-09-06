package ak.tactic.model.graph;

import java.util.LinkedHashSet;
import java.util.Set;

public class Subgraph {
	Set<Node> nodes;
	Set<Link> links;
	
	public Subgraph() {
		nodes = new LinkedHashSet<Node>();
		links = new LinkedHashSet<Link>();
	}
	
	public Subgraph(Set<Node> nodes, Set<Link> links) {
		this.nodes = nodes;
		this.links = links;
	}
}
