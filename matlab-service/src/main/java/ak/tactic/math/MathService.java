package ak.tactic.math;

import java.util.List;

public interface MathService {
	public DiscreteProbDensity filter(DiscreteProbDensity a,DiscreteProbDensity b);
	public DiscreteProbDensity deconvreg(DiscreteProbDensity a,DiscreteProbDensity b);
	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw, Double shape);
	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw);
	public ParametricDensity getGevParamFit(DiscreteProbDensity a);
	public ParametricDensity getGpParamFit(DiscreteProbDensity a);
	public ParametricDensity getNormParamFit(DiscreteProbDensity a);
	public DiscreteProbDensity movingAverage(DiscreteProbDensity a, double span);
	public DiscreteProbDensity gev(double k,double scale,double location);
	public DiscreteProbDensity gp(double k,double scale,double location);
	public DiscreteProbDensity norm(double mean,double std,double extra);
	public DiscreteProbDensity gaussian(double mean,double sd);
	public DiscreteProbDensity multiDistribute(List<DiscreteProbDensity> pdfVector,List<Double> probVector);
	public DiscreteProbDensity newDiscreteProbDensity();
}