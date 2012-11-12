package ak.tactic.model.simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.simulator.framework.Bus;

public class SystemClock {
	Logger log = LoggerFactory.getLogger(SystemClock.class);
	
	TreeSet<Clock> clocks = new TreeSet<Clock>();
	Map<Scheduler, Clock> byScheduler = new HashMap<>();
	
	Set<Scheduler> waiters = new HashSet<Scheduler>();
	TreeSet<Long> stepTime = new TreeSet<Long>();
	Scheduler minSched;
	long timestamp = 0;
	
	class Clock implements Comparable<Clock> {
		Scheduler scheduler;
		long timestamp;
		boolean runnable = true;
		
		public Clock(Scheduler scheduler, long timestamp) {
			this.scheduler = scheduler;
			this.timestamp = timestamp;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
		
		public void setRunnable(boolean runnable) {
			this.runnable = runnable;
		}
		
		public boolean isRunnable() {
			return runnable;
		}
		
		@Override
		public int compareTo(Clock o) {
			if (scheduler.name.equals(o.scheduler.name)) {
				return 0;
			}
			if (timestamp < o.timestamp) return -1;
			else if (timestamp > o.timestamp) return 1;
			else return scheduler.name.compareTo(o.scheduler.name);
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Clock)obj).scheduler.equals(scheduler);
		}
		
		@Override
		public int hashCode() {
			return 37+71*scheduler.name.hashCode();
		}
		
		@Override
		public String toString() {
			return "Clock "+hashCode();
		}
	}
	
	public long getTimestamp() {
		synchronized(clocks) {
			if (!clocks.isEmpty()) {
				timestamp = clocks.first().getTimestamp();
				while (!clocks.first().isRunnable()) {
					clocks.remove(clocks.first());
					if (clocks.isEmpty()) {
						break;
					}
					timestamp = clocks.first().getTimestamp();
				}
			}
			log.info("Global clock: {} next {} set {} wait {} runnable {}", 
				new Object[] {timestamp, (stepTime.isEmpty())?0:stepTime.first(), clocks.size(), waiters.size(), getRunnableCount()});			
		}
		return timestamp;
	}
	
	public void waitUntil(Scheduler scheduler, long expectedTime) {
		setRunnable(scheduler, true);
		synchronized(stepTime) {
			stepTime.add(expectedTime);
		}
		synchronized(waiters) {
			this.waiters.add(scheduler);
			if (waiters.size() >= getRunnableCount()) {
				long clockTime = getTimestamp();
				// release all locks
				final Set<Long> lesserTime = stepTime.headSet(clockTime);
				stepTime.removeAll(lesserTime);
				Long nextStep = stepTime.first();
				while (nextStep <= clockTime) {
					stepTime.remove(nextStep);
					if (stepTime.isEmpty()) {
						break;
					}
					nextStep = stepTime.first();
				}
				for (Scheduler waiter:waiters) {
					setSchedulerClock(waiter, nextStep);
				}
				waiters.clear();
			}
			
		}
	}
	
	public void setSchedulerClock(Scheduler scheduler, long localClock) {
		Clock clock = new Clock(scheduler, localClock);
		synchronized(clocks) {
			Clock existing = byScheduler.get(scheduler);
			if (existing != null) {
				clocks.remove(existing);
			}
			clocks.add(clock);
			byScheduler.put(scheduler,clock);
		}
	}
	
	private int getRunnableCount() {
		synchronized(clocks) {
			int count = 0;
			for (Clock clock : clocks) {
				if (clock.isRunnable()) count++;
			}
			return count;
		}
	}
	
	public void setRunnable(Scheduler scheduler, boolean runnable) {
		synchronized(clocks) {
			Clock targetClock = byScheduler.get(scheduler);
			if (targetClock == null) {
				log.error("Cannot find clock for {}", scheduler);
			}
			targetClock.setRunnable(runnable);
		}
	}
	
	public boolean isRunnable(long localTime) {
		boolean runnable = localTime <= getTimestamp();
		if (!runnable) {
			synchronized(clocks) {
				boolean canRun = true;
				for (Clock clock : clocks) {
					canRun = canRun && !clock.isRunnable(); 
				}
			}
		}
		return runnable;
	}
	
	public static void main(String[] args) {
		SystemClock sys = new SystemClock();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		Bus bus = new Bus(executor);
		Scheduler s1 = new Scheduler(bus, "s1", sys);
		Scheduler s2 = new Scheduler(bus, "s2", sys);
		sys.setSchedulerClock(s1, 0);
		sys.setSchedulerClock(s2, 0);
		sys.setSchedulerClock(s1, 10);
		System.out.println(sys.getTimestamp());
		System.out.println(sys.isRunnable(1));
	}
}
