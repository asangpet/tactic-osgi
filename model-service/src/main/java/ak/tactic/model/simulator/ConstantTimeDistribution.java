package ak.tactic.model.simulator;


public class ConstantTimeDistribution extends TimeDistribution{
	double value;
	
	public ConstantTimeDistribution(double value) {
		this.value = value;
	}
	
	@Override
	public long generate() {
		return Math.round(value);
	}
}
