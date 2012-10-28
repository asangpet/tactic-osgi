package ak.tactic.model.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.VmScheduleEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Vm {
	Logger logger = LoggerFactory.getLogger(Vm.class);	
	int runnerId;
	Bus bus;

	public Vm(int runnerId, Bus bus) {
		this.runnerId = runnerId;
		this.bus = bus;
	}
	
	public void setRunnerId(int runnerId) {
		this.runnerId = runnerId;
	}
	
	@Subscribe
	public void processRequest(VmScheduleEvent e, Object callback) {
		logger.info("VM {} processing {}", runnerId, e);
		logger.info("callback {}", callback);
		RequestArrivalEvent req = e.getRequest();
		req.setProcessingTime(req.getProcessingTime() - 1000);
		bus.send(req, callback);
	}
}
