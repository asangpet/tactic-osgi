package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Scheduler;

public class ReadyEvent {
	Scheduler readyScheduler;
	
	public Scheduler getReadyScheduler() {
		return readyScheduler;
	}
	
	public ReadyEvent(Scheduler scheduler) {
		this.readyScheduler = scheduler;
	}
}
