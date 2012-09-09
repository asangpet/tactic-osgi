package ak.tactic.model.template;

import java.util.Collection;
import java.util.Map;

import ak.tactic.model.deployment.Host;
import ak.tactic.model.deployment.VirtualMachine;
import ak.tactic.model.graph.AnalysisGraph;

public interface AnalysisInstance {
	
	AnalysisGraph getAnalysisGraph();
	Map<String, double[]> analyze();
	Map<Host,Collection<VirtualMachine>> calculatePlacement();
}
