package ak.tactic.model.graph;

import ak.tactic.model.deployment.Component;
import ak.tactic.model.deployment.Dependency;

public class AsynchronousDependency extends Dependency {
	public AsynchronousDependency(Component initiator, Component upstream) {
		super("async",initiator,upstream);
	}
}
