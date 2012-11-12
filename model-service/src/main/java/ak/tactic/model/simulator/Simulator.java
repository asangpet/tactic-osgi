package ak.tactic.model.simulator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
	Worker entry;
	
	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		bus = new Bus(executor);
		
		SystemClock clock = new SystemClock();
		
		Scheduler host1 = new Scheduler(bus, "host1", clock);
		Scheduler host2 = new Scheduler(bus, "host2", clock);
		Scheduler host3 = new Scheduler(bus, "host3", clock);
		
		Reporter reporter = new Reporter();
		bus.register(reporter);
		bus.register(this);
		
		Worker mysql   = new Worker(4, bus, host1, "mysql").setConstantProcessingTime(10);
		Worker solr    = new Worker(5, bus, host2, "solr").setConstantProcessingTime(10);
		Worker memcache= new Worker(6, bus, host3, "memcache").setConstantProcessingTime(10);

		Worker drupal1 = new Balancer (1, bus, host1, "drupal1", mysql, solr, memcache).setConstantProcessingTime(10);
		Worker drupal2 = new Balancer (2, bus, host2, "drupal2", mysql, solr, memcache).setConstantProcessingTime(10);
		Worker drupal3 = new Balancer (3, bus, host3, "drupal3", mysql, solr, memcache).setConstantProcessingTime(10);
		Worker nfs     = new Worker(7, bus, host1, "nfs").setConstantProcessingTime(10);
		
		Worker varnish = new Balancer(0, bus, host2, "varnish", drupal1, drupal2, drupal3, nfs).setConstantProcessingTime(10);
		this.entry = varnish;
		
		/*
		Worker w2 = new Worker(1,bus,host1,"w2");
		Worker w3 = new Worker(2,bus,host2,"w3");
		Worker w1 = new Balancer(0, bus, host1, "w1", w2, w3);
		this.entry = w1;
		*/
		
		bus.post(new StartEvent());
		
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		bus.post(new ReportEvent());
		host1.shutdown();
		host2.shutdown();
		host3.shutdown();
		try {
			executor.awaitTermination(15, TimeUnit.SECONDS);
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
	
	Set<Scheduler> readySet = Collections.synchronizedSet(new HashSet<Scheduler>());
	@Subscribe
	public void populateRequest(ReadyEvent e) {
		log.info("Ready {}",e.getReadyScheduler());
		
		readySet.add(e.getReadyScheduler());
		if (readySet.size() == 2) {
			genRequest(entry.getRunnerId(),500,100);
		}
	}
	
	int sampleCount = 1;
	private void genRequest(final int runnerId, final int meanArrival, final int meanProcessing) {
		final ExponentialDistribution interarrivalDist = new ExponentialDistribution(meanArrival);
		final ExponentialDistribution processingDist = new ExponentialDistribution(meanProcessing);
		double[] intervals = interarrivalDist.sample(sampleCount);
		double[] processings = processingDist.sample(sampleCount);
		long start = 0;
		int index = 0;
		for (double interval : intervals) {
			start += Math.round(interval);
			//long processingTime = Math.round(processings[index]);
			long processingTime = 50;
			entry.getScheduler().addRequest(new RequestArrivalEvent(start, processingTime, runnerId));
			index++;
		}				
	}
	
	public static void main(String[] args) {
		new Simulator().run();
	}
}
