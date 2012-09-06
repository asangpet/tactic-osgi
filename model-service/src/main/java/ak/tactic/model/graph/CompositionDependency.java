package ak.tactic.model.graph;

import ak.tactic.model.deployment.Component;
import ak.tactic.model.deployment.Dependency;

public class CompositionDependency extends Dependency{
	public CompositionDependency(Component initiator, Component upstream) {
		super("comp",initiator,upstream);
	}
}
