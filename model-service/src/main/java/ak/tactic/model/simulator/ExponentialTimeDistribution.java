package ak.tactic.model.simulator;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class ExponentialTimeDistribution extends TimeDistribution{
	double mean;
	
	public ExponentialTimeDistribution(double mean) {
		this.mean = mean;
		distribution = new ExponentialDistribution(mean);
	}
}
