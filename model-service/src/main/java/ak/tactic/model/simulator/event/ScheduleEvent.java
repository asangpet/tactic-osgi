package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Token;

public class ScheduleEvent {
	Token token;
	
	public ScheduleEvent() {
	}
	
	public ScheduleEvent(Token token) {
		this.token = token;
	}
	
	public Token getToken() {
		return token;
	}
	
	public void setToken(Token token) {
		this.token = token;
	}
	
	@Override
	public String toString() {
		return "Schedule event token:"+token;
	}
}
