package ak.tactic.model.simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.framework.Bus;

public class Balancer extends Worker {
	final List<Worker> workers = new ArrayList<>();
	Iterator<Worker> workerIterator;
	
	public Balancer(int runnerId, Bus bus, Scheduler scheduler) {
		super(runnerId, bus, scheduler);
	}
	
	public void setDownstream(Collection<Worker> workers) {
		this.workers.clear();
		this.workers.addAll(workers);
	}

	@Override
	protected void postProcess(RequestCompleteEvent completedRequest) {
		RequestArrivalEvent origin = completedRequest.getOrigin();
		if (origin.getTargetRunner() == runnerId) {
			if (workerIterator == null || !workerIterator.hasNext()) {
				workerIterator = workers.iterator();
			}
			
			int targetRunner = workerIterator.next().getRunnerId();
			origin.resetRequest(completedRequest.getFinishTime(), 30, targetRunner);
			bus.post(origin);
		}
	}
}
