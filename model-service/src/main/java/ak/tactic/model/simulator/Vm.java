package ak.tactic.model.simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.event.ScheduleEvent;
import ak.tactic.model.simulator.event.VmScheduleEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Vm {
	Logger logger = LoggerFactory.getLogger(Vm.class);	
	int runnerId;
	BlockingQueue<RequestArrivalEvent> requestQueue = new LinkedBlockingQueue<>();
	Bus bus;
	Scheduler scheduler;
	boolean runnable;

	public Vm(int runnerId, Bus bus, Scheduler scheduler) {
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
	
	@Subscribe
	public void processRequest(VmScheduleEvent e, Object callback) {
		int runtime = 10;
		Token token = e.getToken();
		RequestArrivalEvent request = requestQueue.peek();
		logger.debug("VM {} scheduling {} request {}", new Object[] {runnerId, e, request});
		
		if (request != null) {
			if (request.getStartTime() == RequestArrivalEvent.DEFAULT_START_TIME) {
				request.setStartTime(token.getRuntime());
			}
			
			if (request.getProcessingTime() < runtime) {
				token.addRuntime(request.getProcessingTime());
			} else {
				token.addRuntime(runtime);
			}
			long remainingTime = request.getProcessingTime() - runtime;
			request.setProcessingTime(remainingTime);
			if (remainingTime <= 0) {
				try {
					requestQueue.take();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if (requestQueue.isEmpty()) {
					this.runnable = false;
				}
				bus.post(new RequestCompleteEvent()
					.setStartTime(request.getStartTime())
					.setFinishTime(token.getRuntime())
					.setProcessingTime(request.getProcessingTime()));
			}
		}
		bus.send(new ScheduleEvent(token), callback);
	}
}