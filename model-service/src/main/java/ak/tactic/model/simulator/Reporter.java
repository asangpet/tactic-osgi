package ak.tactic.model.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.StartEvent;

import com.google.common.eventbus.Subscribe;

public class Reporter {
	Logger log = LoggerFactory.getLogger(Reporter.class);
	
	@Subscribe
	public void reportStart(StartEvent e) {
		log.info("Reporter {}",e);
	}
}
