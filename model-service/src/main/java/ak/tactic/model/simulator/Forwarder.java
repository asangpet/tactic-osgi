package ak.tactic.model.simulator;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.framework.Bus;

public class Forwarder extends Worker {
	Worker worker;
	
	public Forwarder(int runnerId, Bus bus, Scheduler scheduler, String name) {
		super(runnerId, bus, scheduler, name);
	}
	
	public void setDownstream(Worker worker) {
		this.worker = worker;
	}

	@Override
	protected void postProcess(RequestCompleteEvent completedRequest) {
		RequestArrivalEvent origin = completedRequest.getOrigin();
		if (origin.getTargetRunner() == runnerId) {
			origin.resetRequest(completedRequest.getFinishTime(), 30, worker.getRunnerId());
			bus.post(origin);
		}
	}
}
