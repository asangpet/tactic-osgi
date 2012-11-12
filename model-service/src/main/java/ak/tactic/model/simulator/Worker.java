package ak.tactic.model.simulator;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.event.ScheduleEvent;
import ak.tactic.model.simulator.event.WorkerScheduleEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Worker {
	protected Logger logger = LoggerFactory.getLogger(Worker.class);	
	protected int runnerId;
	protected BlockingQueue<RequestArrivalEvent> requestQueue;
	protected Bus bus;
	protected Scheduler scheduler;
	protected boolean runnable;
	protected TimeDistribution processingTime;
	protected String name;

	public Worker(int runnerId, Bus bus, Scheduler scheduler, String name) {
		this.runnerId = runnerId;
		this.bus = bus;
		this.scheduler = scheduler;
		this.name = name;
		scheduler.addWorker(this);
		
		Comparator<RequestArrivalEvent> comparator = new Comparator<RequestArrivalEvent>() {
			@Override
			public int compare(RequestArrivalEvent o1, RequestArrivalEvent o2) {
				if (o1.getTimestamp() < o2.getTimestamp()) {
					return -1;
				} else if (o1.getTimestamp() > o2.getTimestamp()) {
					return 1;
				} else {
					return o1.hashCode() - o2.hashCode();
				}
			}
		};
		requestQueue = new PriorityBlockingQueue<>(100, comparator);
	}
	
	public Worker setProcessingTime(TimeDistribution dist) {
		processingTime = dist;
		return this;
	}
	
	public Worker setExponentialProcessingTime(double mean) {
		processingTime = new ExponentialTimeDistribution(mean);
		return this;
	}
	
	public Worker setConstantProcessingTime(double mean) {
		processingTime = new ConstantTimeDistribution(mean);
		return this;
	}
	
	public Worker setRunnerId(int runnerId) {
		this.runnerId = runnerId;
		return this;
	}
	
	public void addRequest(RequestArrivalEvent e) {
		if (processingTime != null) {
			e.setProcessingTime(processingTime.generate());
		}
		this.requestQueue.add(e);
		runnable = true;
	}
	
	public boolean isRunnable() {
		return runnable;
	}
	
	public RequestArrivalEvent peek() {
		return requestQueue.peek();
	}
	
	public int getRunnerId() {
		return runnerId;
	}
	
	@Subscribe
	public void processRequest(WorkerScheduleEvent e, Object callback) {
		int allocatedSliceTime = 10;
		int remainingSliceTime = allocatedSliceTime;
		Token token = e.getToken();
		
		while (remainingSliceTime > 0) {
			RequestArrivalEvent request = requestQueue.peek();
			if (request == null || request.getTimestamp() > token.getRuntime()) {
				// no available request, or it's a future request
				break;
			}
			
			logger.info("Worker {} scheduling {} request {}", new Object[] {name, e, request});			
			
			if (request.getProcessedTime() == 0) {
				request.setStartTime(token.getRuntime());
			}

			long remainingWork = request.getProcessingTime() - request.getProcessedTime();
			if (remainingWork < remainingSliceTime) {
				request.process(remainingWork);
				token.addRuntime(remainingWork);
				remainingSliceTime -= request.getProcessedTime();
			} else {
				request.process(remainingSliceTime);
				token.addRuntime(remainingSliceTime);
				remainingSliceTime = 0;
			}
			
			if (request.getProcessedTime() == request.getProcessingTime()) {
				try {
					requestQueue.take();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if (requestQueue.isEmpty()) {
					this.runnable = false;
				}
				
				RequestCompleteEvent completion = new RequestCompleteEvent()
					.setRequestTime(request.getTimestamp())
					.setStartTime(request.getStartTime())
					.setFinishTime(token.getRuntime())
					.setProcessingTime(request.getProcessingTime())
					.setRunner(this)
					.setOrigin(request);

				request.getRequestChain().offer(completion);
				bus.post(completion);

				postProcess(completion);
			}
		}
		bus.send(new ScheduleEvent(token), callback);
	}
	
	protected void postProcess(RequestCompleteEvent completedRequest) {
		// Do nothing
	}
	
	@Override
	public String toString() {
		return "Worker "+name+"("+runnerId+")"; 
	}
	
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	public String getName() {
		return name;
	}
}