package ak.tactic.math;

import one.matlab.tools.Convolution;
import one.matlab.tools.Distribution;
import one.matlab.tools.ParallelConvolution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.toolbox.javabuilder.MWException;

public class MatlabService {
	Logger log = LoggerFactory.getLogger(MatlabService.class);

	int maxTime = ModelConfig.maxTime;
	double offset = ModelConfig.offset;
	int rangeinterval = ModelConfig.rangeinterval;
	double[] rangeArray;

	Convolution conv;
	ParallelConvolution parConv;
	Distribution dist;

	public MatlabService() {
		try {
			conv = new Convolution();
			parConv = new ParallelConvolution();
			dist = new Distribution();
		} catch (MWException e) {
			log.error(e.toString());
		}

		// generate rangeArray
		rangeArray = new double[maxTime/rangeinterval];
		double start = rangeinterval/2.0;
		for (int i=0;i<rangeArray.length;i++) {
			rangeArray[i] = start;
			start = start+rangeinterval;
		}
	}
	
	public Convolution getConv() {
		return conv;
	}
	
	public ParallelConvolution getParConv() {
		return parConv;
	}
	
	public Distribution getDist() {
		return dist;
	}
}
