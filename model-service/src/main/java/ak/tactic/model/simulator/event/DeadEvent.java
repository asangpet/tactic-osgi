package ak.tactic.model.simulator.event;

public class DeadEvent {
	Object source;
	Object event;
	
	public DeadEvent(Object source, Object event) {
		this.source = source;
		this.event = event;
	}
	
	@Override
	public String toString() {
		return "DeadEvent "+source+" "+event;
	}
}
