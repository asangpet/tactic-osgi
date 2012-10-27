package ak.tactic.model.simulator;

import ak.tactic.model.simulator.event.ScheduleEvent;

import com.google.common.eventbus.Subscribe;

public class Vm {
	int runnerId;

	public Vm(int runnerId) {
		this.runnerId = runnerId;
	}
	
	public void setRunnerId(int runnerId) {
		this.runnerId = runnerId;
	}
	
	@Subscribe
	public void processRequest(ScheduleEvent e) {
		e.getToken();
	}
}
