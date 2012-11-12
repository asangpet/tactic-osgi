package ak.tactic.model.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.framework.Bus;

public class Balancer extends Worker {
	final List<Worker> workers = new ArrayList<>();
	Iterator<Worker> workerIterator;
	ReentrantLock workerLock = new ReentrantLock();
	
	public Balancer(int runnerId, Bus bus, Scheduler scheduler, String name) {
		super(runnerId, bus, scheduler, name);
	}
	
	public Balancer(int runnerId, Bus bus, Scheduler scheduler, String name, Worker... workers) {
		super(runnerId, bus, scheduler, name);
		setDownstream(workers);
	}
	
	public void setDownstream(Worker... workers) {
		this.workers.clear();
		this.workers.addAll(Arrays.asList(workers));
	}

	@Override
	protected void postProcess(RequestCompleteEvent completedRequest) {
		RequestArrivalEvent origin = completedRequest.getOrigin();
		if (origin.getTargetRunner() == runnerId) {
			workerLock.lock();
			if (workerIterator == null || !workerIterator.hasNext()) {
				workerIterator = workers.iterator();
			}
			
			int targetRunner = workerIterator.next().getRunnerId();
			origin.resetRequest(completedRequest.getFinishTime(), 30, targetRunner);
			workerLock.unlock();
			
			bus.post(origin);
		}
	}
}
