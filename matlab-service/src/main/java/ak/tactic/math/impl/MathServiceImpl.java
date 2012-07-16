package ak.tactic.math.impl;

import java.util.List;

import one.matlab.tools.Convolution;
import one.matlab.tools.Distribution;
import one.matlab.tools.ParallelConvolution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.math.DiscreteProbDensity;
import ak.tactic.math.MathService;
import ak.tactic.math.ModelConfig;
import ak.tactic.math.ParametricDensity;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

public class MathServiceImpl implements MathService {
	Logger log = LoggerFactory.getLogger(MathServiceImpl.class);

	int maxTime = ModelConfig.maxTime;
	double offset = ModelConfig.offset;
	int rangeinterval = ModelConfig.rangeinterval;
	double[] rangeArray;

	Convolution conv;
	ParallelConvolution parConv;
	Distribution dist;

	public MathServiceImpl() {
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

	@Override
	public DiscreteProbDensity deconvreg(DiscreteProbDensity a,
			DiscreteProbDensity b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity filter(DiscreteProbDensity a,
			DiscreteProbDensity b) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity gaussian(double mean, double sd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametricDensity getGevParamFit(DiscreteProbDensity a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametricDensity getGevParamFit(DiscreteProbDensity a,
			double[] raw, Double shape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametricDensity getGpParamFit(DiscreteProbDensity a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametricDensity getNormParamFit(DiscreteProbDensity a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity gev(double k, double scale, double location) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime/rangeinterval,0,maxTime,offset);
		int start = rangeinterval / 2;

		try {
			MWNumericArray mResult = (MWNumericArray)dist.gev_pdf(1,
					(double)start,
					(double)maxTime-rangeinterval+start,
					(double)rangeinterval,
					k,
					scale,
					location)[0];
			result.setPdf(mResult.getDoubleData());
			mResult.dispose();
		} catch (MWException e) {
			log.error(e.toString());
			return null;
		}

		return result.normalize();
	}

	@Override
	public DiscreteProbDensity gp(double k, double scale, double location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity movingAverage(DiscreteProbDensity a, double span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity multiDistribute(
			List<DiscreteProbDensity> pdfVector, List<Double> probVector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity newDiscreteProbDensity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiscreteProbDensity norm(double mean, double std, double extra) {
		// TODO Auto-generated method stub
		return null;
	}
}
