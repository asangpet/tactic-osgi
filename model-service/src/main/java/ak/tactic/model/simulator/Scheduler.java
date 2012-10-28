package ak.tactic.model.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.ReadyEvent;
import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.ScheduleEvent;
import ak.tactic.model.simulator.event.StartEvent;
import ak.tactic.model.simulator.event.VmScheduleEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Scheduler {
	Logger log = LoggerFactory.getLogger(Scheduler.class);
	List<Vm> virtualMachines = new ArrayList<Vm>();
	BlockingQueue<RequestArrivalEvent> requestQueue = new LinkedBlockingQueue<>();
	Bus bus;
	ExecutorService executor = Executors.newFixedThreadPool(2); 
	
	public Scheduler(Bus bus) {
		this.bus = bus;
		this.bus.register(this);
	}
	
	public void addVm(Vm vm) {
		vm.setRunnerId(virtualMachines.size());
		virtualMachines.add(vm);
		bus.register(vm);
	}
	
	@Subscribe
	public void requestArrive(RequestArrivalEvent e) {
		log.info("Adding request {} to queue", e);
		requestQueue.add(e);
	}
	
	@Subscribe
	public void start(final StartEvent e) {
		log.info("Starting scheduler");
		bus.post(new ReadyEvent());
		
		Token token = new Token();
		try {
			RequestArrivalEvent requestEvent = requestQueue.take();
			bus.post(new ScheduleEvent(token, requestEvent));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		log.info("Started");
	}
	
	@Subscribe
	public void schedule(ScheduleEvent e) {
		log.info("Scheduled: {}",e);
		Vm vm = virtualMachines.get(1);
		bus.send(new VmScheduleEvent(e.getToken(), e.getRequest()), vm, this);
	}
}