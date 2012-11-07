package ak.tactic.model.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.ReadyEvent;
import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.ScheduleEvent;
import ak.tactic.model.simulator.event.StartEvent;
import ak.tactic.model.simulator.event.WorkerScheduleEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Scheduler {
	Logger log = LoggerFactory.getLogger(Scheduler.class);
	List<Worker> workers = new ArrayList<Worker>();
	Bus bus;
	ExecutorService executor = Executors.newFixedThreadPool(2);
	Thread waiterThread = null;
	boolean terminated = false;
	
	public Scheduler(Bus bus) {
		this.bus = bus;
		this.bus.register(this);
	}
	
	public void addVm(Worker vm) {
		vm.setRunnerId(workers.size());
		workers.add(vm);
		bus.register(vm);
	}
	
	public void shutdown() {
		terminated = true;
	}
	
	@Subscribe
	public void requestArrive(RequestArrivalEvent e) {
		log.trace("Adding request {} to queue", e);
		workers.get(e.getTargetRunner()).addRequest(e);
		if (waiterThread != null) {
			waiterThread.interrupt();
		}
	}
	
	@Subscribe
	public void start(final StartEvent e) {
		log.info("Starting scheduler");
		bus.post(new ReadyEvent());
		
		bus.post(new ScheduleEvent());
		log.info("Started");
	}
	
	@Subscribe
	public void schedule(ScheduleEvent e) {
		log.trace("Scheduled: {}",e);
		Token token = e.getToken();
		if (token == null) {
			token = new Token();
		} else {
			int runner = token.getRunnerId() + 1;
			if (runner == workers.size()) {
				runner = 0;
			}
			token.setRunnerId(runner);
		}
		
		Worker runner = null;
		while (runner == null && !terminated) {
			runner = findRunner(token);
			if (terminated) {
				return;
			}
			if (runner == null) {
				waiterThread = Thread.currentThread();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					waiterThread = null;
					log.info("Waiter interrupted");
				}
			}
		}
	}
	
	private Worker findRunner(Token token) {
		RequestArrivalEvent earliestRequest = null;
		int initRunner = token.getRunnerId();
		do {
			Worker vm = workers.get(token.getRunnerId());
			if (vm.isRunnable()) {
				RequestArrivalEvent request = vm.peek();
				if (request != null) {
					if (request.getTimestamp() <= token.getRuntime()) {
						bus.send(new WorkerScheduleEvent(token), vm, this);
						return vm;
					}
					if (earliestRequest == null || earliestRequest.getTimestamp() < request.getTimestamp()) {
						earliestRequest = request;
					}
				}
			}
			token.setRunnerId(token.getRunnerId()+1);
			if (token.getRunnerId() == workers.size()) {
				token.setRunnerId(0);
			}
		} while (token.getRunnerId() != initRunner);
		
		if (earliestRequest != null) {
			token.setRuntime(earliestRequest.getTimestamp());
			Worker vm = workers.get(earliestRequest.getTargetRunner()); 
			bus.send(new WorkerScheduleEvent(token), vm, this);
			return vm;
		}
		return null;
	}
}