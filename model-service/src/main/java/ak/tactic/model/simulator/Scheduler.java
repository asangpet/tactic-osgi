package ak.tactic.model.simulator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
	LinkedHashMap<Integer, Worker> workers = new LinkedHashMap<Integer,Worker>();
	Bus bus;
	ExecutorService executor = Executors.newFixedThreadPool(2);
	Thread waiterThread = null;
	boolean terminated = false;
	final String name;
	final SystemClock clock;
	
	public Scheduler(Bus bus, String name, SystemClock clock) {
		this.bus = bus;
		this.bus.register(this);
		this.name = name;
		this.clock = clock;
		clock.setSchedulerClock(this, 0);
	}
	
	public void addWorker(Worker worker) {
		workers.put(worker.getRunnerId(), worker);
		bus.register(worker);
	}
	
	public void shutdown() {
		terminated = true;
	}
	
	@Subscribe
	public void addRequest(RequestArrivalEvent e) {
		Worker worker = workers.get(e.getTargetRunner());
		if (worker != null) {
			log.info("{} - {} add request {} to queue", new Object[] { this, worker, e });
			worker.addRequest(e);
			if (waiterThread != null) {
				waiterThread.interrupt();
			}
		}
	}
	
	@Subscribe
	public void start(final StartEvent e) {
		log.info("Starting scheduler");
		bus.post(new ReadyEvent(this));
		
		bus.post(new ScheduleEvent());
		log.info("Started");
	}
	
	@Subscribe
	public void schedule(ScheduleEvent e) {
		log.trace("Scheduled: {}",e);
		Token token = e.getToken();
		if (token == null) {
			token = new Token();
			token.setRunnerId(nextRunner().runnerId);
		} else {
			Worker runner = nextRunner();
			token.setRunnerId(runner.getRunnerId());
		}
		clock.setSchedulerClock(this, token.getRuntime());
		
		Worker runner = null;
		while (runner == null && !terminated) {
			runner = findRunner(token);
			if (terminated) {
				return;
			}
			if (runner == null) {
				waiterThread = Thread.currentThread();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					waiterThread = null;
					log.trace("Waiter interrupted");
				}
			}
		}
	}
	
	private Worker findRunner(Token token) {
		RequestArrivalEvent earliestRequest = null;
		int initRunner = token.getRunnerId();
		Worker worker = nextRunner();
		while (worker.getRunnerId() != initRunner) {
			worker = nextRunner();
		}
		do {
			if (worker.isRunnable()) {
				RequestArrivalEvent request = worker.peek();
				if (request != null) {
					if ((request.getTimestamp() <= token.getRuntime()) && (clock.isRunnable(request.getTimestamp()))) {
						bus.send(new WorkerScheduleEvent(token), worker, this);
						return worker;							
					}
					if (earliestRequest == null || earliestRequest.getTimestamp() < request.getTimestamp()) {
						earliestRequest = request;
					}
				}
			}
			worker = nextRunner();
			token.setRunnerId(worker.getRunnerId());
		} while (token.getRunnerId() != initRunner);
		
		if (earliestRequest != null) {			
			clock.waitUntil(this, earliestRequest.getTimestamp());
			if (clock.isRunnable(earliestRequest.getTimestamp())) {
				token.setRunnerId(earliestRequest.getTargetRunner());
				token.setRuntime(earliestRequest.getTimestamp());
				Worker earlyworker = workers.get(earliestRequest.getTargetRunner()); 
				bus.send(new WorkerScheduleEvent(token), earlyworker, this);
				return worker;				
			}
		} else {
			clock.setRunnable(this, false);
		}
		return null;
	}
	
	Iterator<Map.Entry<Integer, Worker>> runnerIterator;
	synchronized private Worker nextRunner() {
		// Warning: may run to concurrent modification
		if (runnerIterator == null || !runnerIterator.hasNext()) {
			runnerIterator = workers.entrySet().iterator();
		}
		return runnerIterator.next().getValue();
	}
	
	@Override
	public String toString() {
		return "Scheduler "+name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return name.equals(((Scheduler)obj).name);
	}
}