package ak.tactic.model.graph;

import ak.tactic.model.deployment.Component;
import ak.tactic.model.deployment.Entity;

public class Dependency extends Entity {
	public Component upstream;
	public Component initiator;
	
	public Double distProb;
	
	public Dependency(String type,Component initiator, Component upstream) {
		super(type+"-"+initiator.getName()+"-"+upstream.getName());
		this.upstream = upstream;
		this.initiator = initiator;
	}
	
	@Override
	public String toString() {
		String result = name+" = { "+initiator+"->"+upstream+" }";
		return result;
	}
}
