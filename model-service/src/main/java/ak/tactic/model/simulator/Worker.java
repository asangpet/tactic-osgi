package ak.tactic.model.simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	protected BlockingQueue<RequestArrivalEvent> requestQueue = new LinkedBlockingQueue<>();
	protected Bus bus;
	protected Scheduler scheduler;
	protected boolean runnable;

	public Worker(int runnerId, Bus bus, Scheduler scheduler) {
		this.runnerId = runnerId;
		this.bus = bus;
		this.scheduler = scheduler;
	}
	
	public void setRunnerId(int runnerId) {
		this.runnerId = runnerId;
	}
	
	public void addRequest(RequestArrivalEvent e) {		
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
			
			logger.trace("Worker {} scheduling {} request {}", new Object[] {runnerId, e, request});			
			
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
}