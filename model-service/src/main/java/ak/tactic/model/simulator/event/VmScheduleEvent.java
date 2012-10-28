package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Token;

public class VmScheduleEvent extends ScheduleEvent {

	public VmScheduleEvent(Token token, RequestArrivalEvent request) {
		super(token, request);
	}
	
	@Override
	public String toString() {
		return "VM Schedule event token:"+token+" req:"+request;
	}
	
}
