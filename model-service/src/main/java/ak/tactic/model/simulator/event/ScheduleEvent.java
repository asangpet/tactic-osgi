package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Token;

public class ScheduleEvent {
	Token token;
	RequestArrivalEvent request;
	
	public ScheduleEvent(Token token, RequestArrivalEvent request) {
		this.token = token;
		this.request = request;
	}
	
	public Token getToken() {
		return token;
	}
	
	@Override
	public String toString() {
		return "Schedule event "+token+" - "+request;
	}
}
