package ak.tactic.model.data;

import ak.tactic.math.DiscreteProbDensity;

public class DistributionData {
	public String name;
	public double[] data;
	
	public DistributionData(String name, DiscreteProbDensity prob) {
		this.name = name;
		this.data = prob.getPdf();
	}
}
