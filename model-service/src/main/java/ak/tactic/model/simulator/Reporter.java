package ak.tactic.model.simulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.math.DiscreteProbDensity;
import ak.tactic.model.simulator.event.ReportEvent;
import ak.tactic.model.simulator.event.RequestCompleteEvent;
import ak.tactic.model.simulator.framework.Subscribe;

public class Reporter {
	Logger log = LoggerFactory.getLogger(Reporter.class);
	Map<Worker,DiscreteProbDensity> densities = new ConcurrentHashMap<Worker, DiscreteProbDensity>();
	AtomicInteger count = new AtomicInteger(0);
	
	DiscreteProbDensity createDensity() {
		return new DiscreteProbDensity(1000,0,60000,0);
	}
	
	@Subscribe
	public void reportRequest(RequestCompleteEvent e) {
		synchronized(densities) {
			DiscreteProbDensity pdf = densities.get(e.getRunner());
			if (pdf == null) {
				pdf = createDensity();
				densities.put(e.getRunner(), pdf);
			}
			pdf.add(e.getFinishTime()-e.getRequestTime());
			log.trace("RequestCompleted {}",e);
			int currentCount = count.incrementAndGet();
			if (currentCount % 1000 == 0) {
				log.info("Processed {} requests",count);
			}
		}
	}
	
	@Subscribe
	public void reportRequest(ReportEvent e) {
		for (Map.Entry<Worker, DiscreteProbDensity> entry : densities.entrySet()) {
			log.info("pdf_{} = {}",entry.getKey().getName(), toString(entry.getValue()));
		}
	}
	
	String toString(DiscreteProbDensity density) {
		StringBuilder builder = new StringBuilder().append("[");
		double[] pdf = density.getPdf();
		for (int i=0;i<pdf.length;i++) {
			builder.append(" ").append(pdf[i]);
		}
		builder.append("]");
		return builder.toString();
	}
}
