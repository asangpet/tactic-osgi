package ak.tactic.model.graph;

import ak.tactic.model.deployment.Component;

public class CompositionDependency extends Dependency{
	public CompositionDependency(Component initiator, Component upstream) {
		super("comp",initiator,upstream);
	}
}
