package ak.tactic.model.graph;

import ak.tactic.model.math.DiscreteProbDensity;
import ak.tactic.model.math.ParametricDensity;

public class NodeModel {
	TransferFunction transfer;
	double cutoff;
	
	DiscreteProbDensity pdf;
	ParametricDensity outputResponse;
	double[] param;
	double rawCount;
	
	public NodeModel() {}
	
	public NodeModel(DiscreteProbDensity pdf, double[] param, double rawCount) {
		this.pdf = pdf;
		this.param = param;
		this.rawCount = rawCount;
	}
	
	public NodeModel(ParametricDensity outputResponse) {
		this.outputResponse = outputResponse;
	}
	
	public TransferFunction getTransfer() {
		return transfer;
	}

}
