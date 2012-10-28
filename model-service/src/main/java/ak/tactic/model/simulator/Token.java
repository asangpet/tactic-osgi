package ak.tactic.model.simulator;

public class Token {
	long runtime = 0;
	int runnerId = 0;
	
	public void addRuntime(long time) {
		runtime += time;
	}
	
	
	public void setRunnerId(int runnerId) {
		this.runnerId = runnerId;
	}
	
	public int getRunnerId() {
		return runnerId;
	}
	
	@Override
	public String toString() {
		return "{ Token runner:"+runnerId+" runtime:"+runtime+" }";
	}
}
