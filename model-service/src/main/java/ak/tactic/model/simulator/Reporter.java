package ak.tactic.model.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.framework.Subscribe;

public class Reporter {
	Logger log = LoggerFactory.getLogger(Reporter.class);
	
	@Subscribe
	public void reportRequest(RequestCompleteEvent e) {
		log.info("RequestCompleted {}",e);
	}
}
