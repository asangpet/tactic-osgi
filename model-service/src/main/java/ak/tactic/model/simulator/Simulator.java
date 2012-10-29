package ak.tactic.model.simulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.ReadyEvent;
import ak.tactic.model.simulator.event.RequestArrivalEvent;
import ak.tactic.model.simulator.event.StartEvent;
import ak.tactic.model.simulator.framework.Bus;
import ak.tactic.model.simulator.framework.Subscribe;

public class Simulator {
	static Logger log = LoggerFactory.getLogger(Simulator.class);
	static int DISPATCH_QUEUE_LENGTH = 1000;
	Bus bus;
	
	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		bus = new Bus(executor);
		Scheduler scheduler = new Scheduler(bus);
		Reporter reporter = new Reporter();
		bus.register(reporter);
		bus.register(this);
		
		Vm vm1 = new Vm(1, bus, scheduler);
		Vm vm2 = new Vm(2, bus, scheduler);
		scheduler.addVm(vm1);
		scheduler.addVm(vm2);
		bus.post(new StartEvent());
		
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdownNow();
		/*
		Token token = new Token();
		RequestArrivalEvent requestEvent = new RequestArrivalEvent();
		scheduler.eventBus.post(new StartEvent());
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException ioe) {}
		executor.shutdown();
		*/
	}
	
	@Subscribe
	public void populateRequest(ReadyEvent e) {
		bus.post(new RequestArrivalEvent(0, 20, 0));
		bus.post(new RequestArrivalEvent(0, 20, 1));
	}
	
	public static void main(String[] args) {
		new Simulator().run();
	}
}
