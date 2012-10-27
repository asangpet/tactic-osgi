package ak.tactic.model.simulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.StartEvent;
import ak.tactic.model.simulator.framework.Bus;

public class Simulator {
	static Logger log = LoggerFactory.getLogger(Simulator.class);
	static int DISPATCH_QUEUE_LENGTH = 1000;
	
	public void run() {
		Bus bus = new Bus();
		Scheduler scheduler = new Scheduler(bus);
		Reporter reporter = new Reporter();
		bus.register(reporter);
		
		Vm vm1 = new Vm(0);
		Vm vm2 = new Vm(1);
		scheduler.addVm(vm1);
		scheduler.addVm(vm2);
		bus.post(new StartEvent());
		
		ExecutorService executor = bus.getExecutor();
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
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
	
	public static void main(String[] args) {
		log.info("Hello");
		new Simulator().run();
	}
}
