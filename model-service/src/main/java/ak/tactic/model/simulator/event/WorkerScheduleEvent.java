package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Token;

public class WorkerScheduleEvent extends ScheduleEvent {
	public WorkerScheduleEvent(Token token) {
		super(token);
	}
	
	@Override
	public String toString() {
		return "Worker schedule event token:"+token;
	}
	
}
