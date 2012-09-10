package ak.tactic.mathclient;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ak.tactic.math.MatlabService;

@Component
public class HelloClient {
	@Autowired MatlabService mathService;
	
	Logger log = LoggerFactory.getLogger(HelloClient.class);
	
	@PostConstruct
	public void go() {
		//DiscreteProbDensity density = mathService.gev(0.5,200,10);
		//log.info("Density for gev 0.5/200/10 = "+density.average());
	}
}
