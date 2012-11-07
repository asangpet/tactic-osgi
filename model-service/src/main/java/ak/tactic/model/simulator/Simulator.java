package ak.tactic.model.simulator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.event.ReadyEvent;
import ak.tactic.model.simulator.event.ReportEvent;
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
		
		Worker vm1 = new Worker(1, bus, scheduler);
		Worker vm2 = new Worker(2, bus, scheduler);
		scheduler.addVm(vm1);
		scheduler.addVm(vm2);
		bus.post(new StartEvent());
		
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bus.post(new ReportEvent());
		scheduler.shutdown();
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
		genRequest(0,500,100);
		genRequest(1,1000,100);
	}
	
	int sampleCount = 10000;
	private void genRequest(final int runnerId, final int meanArrival, final int meanProcessing) {
		new Thread() {
			@Override
			public void run() {
				final ExponentialDistribution interarrivalDist = new ExponentialDistribution(meanArrival);
				final ExponentialDistribution processingDist = new ExponentialDistribution(meanProcessing);
				double[] intervals = interarrivalDist.sample(sampleCount);
				double[] processings = processingDist.sample(sampleCount);
				long start = 0;
				int index = 0;
				for (double interval : intervals) {
					start += Math.round(interval);
					long processingTime = Math.round(processings[index]);
					bus.post(new RequestArrivalEvent(start, processingTime, runnerId));
					index++;
				}				
			};
		}.start();		
	}
	
	public static void main(String[] args) {
		new Simulator().run();
	}
}
