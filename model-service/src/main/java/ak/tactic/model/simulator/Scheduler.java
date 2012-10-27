package ak.tactic.model.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.ScheduleEvent;
import ak.tactic.model.simulator.event.StartEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

import com.google.common.collect.Queues;
import com.google.common.eventbus.DeadEvent;

public class Scheduler {
	Logger log = LoggerFactory.getLogger(Scheduler.class);
	List<Vm> virtualMachines = new ArrayList<Vm>();
	BlockingQueue<RequestArrivalEvent> requestQueue = Queues.newLinkedBlockingQueue();
	Bus eventBus;
	ExecutorService executor = Executors.newFixedThreadPool(2); 
	
	public Scheduler(Bus bus) {
		eventBus = bus;
		eventBus.register(this);
	}
	
	public void addVm(Vm vm) {
		vm.setRunnerId(virtualMachines.size());
		virtualMachines.add(vm);
	}
	
	@Subscribe
	public void requestArrive(RequestArrivalEvent e) {
		requestQueue.add(e);
	}
	
	@Subscribe
	public void start(final StartEvent e) {
		log.info("Starting scheduler");
		eventBus.post(new ScheduleEvent(new Token(), new RequestArrivalEvent()));
		
		/*
		Token token = new Token();
		try {
			RequestArrivalEvent requestEvent = requestQueue.poll(5, TimeUnit.SECONDS);
			eventBus.post(new ScheduleEvent(token, requestEvent));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		log.info("Started");
		*/
	}
	
	@Subscribe
	public void schedule(ScheduleEvent e) {
		log.info("Scheduled: {}",e);
		eventBus.post(new DeadEvent(e, e));
		
		double result = Math.random();
		log.info("Roll: {}",result);
		if (result > 0.1) {
			Token token = new Token();
			//RequestArrivalEvent requestEvent = requestQueue.poll(5, TimeUnit.SECONDS);
			//eventBus.post(new ScheduleEvent(token, requestEvent));
			eventBus.post(new StartEvent());
			log.info("Started");
		}		
		
	}
	
	@Subscribe
	public void deadEvent(DeadEvent e) {
		log.error("{}",e);
	}
}