package ak.tactic.model.graph;

import ak.tactic.model.deployment.Component;
import ak.tactic.model.math.DiscreteProbDensity;

public class DistributionDependency extends Dependency{
	DiscreteProbDensity distProb = null; 
	
	public DistributionDependency(Component initiator, Component upstream) {
		super("dist",initiator,upstream);
	}
}
