package ak.tactic.model.data;

import ak.tactic.model.math.DiscreteProbDensity;

public class DistributionData {
	public String name;
	public String model;
	public double[] data;
	
	public DistributionData(String model, String name, DiscreteProbDensity prob) {
		this.model = model;
		this.name = name;
		this.data = prob.getPdf();
	}
}
