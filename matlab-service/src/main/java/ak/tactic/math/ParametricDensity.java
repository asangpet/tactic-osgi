package ak.tactic.math;

public class ParametricDensity {
	public DiscreteProbDensity pdf;
	public double[] param;
	public double[] inputparam;
	public double rawCount;
	
	public ParametricDensity() {	
	}
	
	public ParametricDensity(DiscreteProbDensity density, double[] param) {
		this(density,param, 0);
	}
	
	public ParametricDensity(DiscreteProbDensity density, double[] param, double rawCount) {
		this.pdf = density;
		this.param = param;
		this.rawCount = rawCount;
	}
	
	public ParametricDensity(DiscreteProbDensity pdf, double[] param, double[] inputparam) {
		this.pdf = pdf;
		this.param = param;
		this.inputparam = inputparam;
	}
	
	public ParametricDensity(DiscreteProbDensity density, double rawCount) {
		this(density,null,rawCount);
	}
	
	public ParametricDensity(DiscreteProbDensity pdf) {
		this(pdf, null, 0);
	}
	
	public ParametricDensity(ParametricDensity d) {
		this.pdf = new DiscreteProbDensity(d.pdf);
		this.param = d.param;
	}
	
	public DiscreteProbDensity getPdf() {
		return pdf;
	}
	
	public double[] getParam() {
		return param;
	}
	
	public double getRawCount() {
		return rawCount;
	}
	
	public void setRawCount(double rawCount) {
		this.rawCount = rawCount;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (param != null) {
			for (int i=0;i<param.length;i++) {
				buf.append(param[i]+",");
			}
		}
		StringBuffer inbuf = new StringBuffer();
		if (inputparam!= null) {
			for (int i=0;i<inputparam.length;i++) {
				inbuf.append(inputparam[i]+",");
			}
		}
		return "ParametricDensity - count {"+rawCount+"} - param {"+buf.toString()+"} - input - {"+inbuf+"}";		
	}	
}
