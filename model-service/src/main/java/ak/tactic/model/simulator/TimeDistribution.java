package ak.tactic.model.simulator;

import org.apache.commons.math3.distribution.RealDistribution;

public abstract class TimeDistribution {
	protected RealDistribution distribution;
	
	public long generate() {
		return Math.round(distribution.sample());
	}
}
