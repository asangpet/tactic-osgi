package ak.tactic.model.graph;

import ak.tactic.math.DiscreteProbDensity;
import ak.tactic.model.deployment.Component;

public class DistributionDependency extends Dependency{
	DiscreteProbDensity distProb = null; 
	
	public DistributionDependency(Component initiator, Component upstream) {
		super("dist",initiator,upstream);
	}
}
