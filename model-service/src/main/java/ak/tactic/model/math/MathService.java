package ak.tactic.model.math;

import java.util.List;

import javax.annotation.PostConstruct;

import one.matlab.tools.Convolution;
import one.matlab.tools.Distribution;
import one.matlab.tools.ParallelConvolution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ak.tactic.math.MatlabService;
import ak.tactic.model.ModelConfig;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

@Service
public class MathService {
	Logger log = LoggerFactory.getLogger(MathService.class);

	int maxTime = ModelConfig.maxTime;
	double offset = ModelConfig.offset;
	int rangeinterval = ModelConfig.rangeinterval;
	double[] rangeArray;

	Convolution conv;
	ParallelConvolution parConv;
	Distribution dist;
	
	@Autowired
	MatlabService matlabService;

	@PostConstruct
	public void init() {
		conv = matlabService.getConv();
		parConv = matlabService.getParConv();
		dist = matlabService.getDist();
		
		assert conv != null;
		assert parConv != null;
		assert dist != null;
	}

	public DiscreteProbDensity deconv(DiscreteProbDensity a,DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);		
		try {
			MWNumericArray mResult = (MWNumericArray)conv.deconv_master(2,
				from(a.getPdf()),
				from(b.getPdf()))[0];
			result.setPdf(mResult.getDoubleData());
			result.raw = null;
			mResult.dispose();			
		} catch (MWException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * Expand the given distribution range by the given degree * probability.
	 * (E.g. degree 2 expansion of a distribution with 0.5 probability will
	 * result in the mean X shifted to 2X.
	 * @param a
	 * @param degree
	 * @param probability
	 * @return
	 */
	public DiscreteProbDensity expand(DiscreteProbDensity a, double degree, double probability) {
		DiscreteProbDensity result = new DiscreteProbDensity(a);
		result.raw = null;
		double sum = 0;
		double factor = degree*probability;
		int lastIndex = 0;
		result.pdf[0] = a.pdf[0];
		for (int i=1; i<result.pdf.length;i++){
			int index = (int)Math.round(i*factor);
			double target = a.pdf[i];
			double prev = a.pdf[i-1];
			if (index < result.pdf.length) {
				result.pdf[index] = target;
				for (int interim = lastIndex+1;interim < index;interim++) {
					result.pdf[interim] = prev + (target - prev)*(interim-lastIndex)/(index-lastIndex);					
				}
				lastIndex = index;
			} else {
				for (int interim = lastIndex+1;interim < result.pdf.length;interim++) {
					result.pdf[interim] = prev + (target - prev)*(interim-lastIndex)/(index-lastIndex);
				}				
				break;
			}
		}
		for (double d : result.pdf) {
			sum+=d;
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;		
	}
	
	/**
	 * Shrink the given distribution range by the given degree * probability.
	 * (E.g. degree 2 shrinking of a distribution with 1.0 probability will
	 * result in the mean X shifted to X/2.
	 * @param a
	 * @param degree
	 * @param probability
	 * @return
	 */
	public DiscreteProbDensity shrink(DiscreteProbDensity a, double degree, double probability) {
		DiscreteProbDensity result = new DiscreteProbDensity(a);
		result.raw = null;
		double sum = 0;
		double factor = 1.0/degree/probability;
		int lastIndex = 0;
		result.pdf[0] = a.pdf[0];
		for (int i=1; i<result.pdf.length;i++){
			int index = (int)Math.round(i*factor);
			double target = a.pdf[i];
			double prev = a.pdf[i-1];
			if (index == lastIndex) {
				result.pdf[index] += target;
			} else {
				result.pdf[index] = target;
			}
			lastIndex = index;
		}
		for (int i=lastIndex+1; i<result.pdf.length; i++) {
			// Fading out factor
			//result.pdf[i] = result.pdf[i-1]/2;
			result.pdf[i] = 0;
		}
		for (double d : result.pdf) {
			sum+=d;
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;		
	}	

	public DiscreteProbDensity filter(DiscreteProbDensity a,DiscreteProbDensity b) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);		
		try {
			MWNumericArray mResult = (MWNumericArray)conv.conv_cut(1,
				from(a.getPdf()),
				from(b.getPdf()))[0];
			result.setPdf(mResult.getDoubleData());
			result.raw = null;
			mResult.dispose();			
		} catch (MWException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public DiscreteProbDensity gaussian(double mean, double sd) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	/*
	int rawfitcount = 0;
	int totalfitcount = 0;
	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw, Double shape) {
		DiscreteProbDensity result = new DiscreteProbDensity(a.numSlots,a.min,a.max,a.offset);
		double[] myfit = null;
		if (raw != null) {
			myfit = MxFunction.gevfit(raw);
			int start = rangeinterval/2;
			double[] matlabResult = MxFunction.gevpdf(start, maxTime-rangeinterval+start, rangeinterval, myfit[0], myfit[1], myfit[2]);
			result.pdf = matlabResult;
			rawfitcount++;
		} else {
			//myfit = MxFunction.gevfit(a.generateRaw())
			if (shape == null) {
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf);
			} else {
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf,shape);
				myfit = MxFunction.gevfitpdf(maxTime/rangeinterval,rangeArray,a.pdf);
			}
			double[] matlabResult = MxFunction.gevpdf(rangeArray, myfit[0], myfit[1], myfit[2]);
			result.pdf = matlabResult;
		}
		totalfitcount++;
		log.info("***** Fitting raw: {} / {} : total", rawfitcount, totalfitcount);
		return new ParametricDensity(result.normalize(), myfit);
	}
	*/
	
	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw, Double shape) {
		throw new NotImplementedException();
	}

	public ParametricDensity getGevParamFit(DiscreteProbDensity a, double[] raw) {
		throw new NotImplementedException();
	}
	public ParametricDensity getGevParamFit(DiscreteProbDensity a) {
		throw new NotImplementedException();
	}
	
	public ParametricDensity getNormParamFit(DiscreteProbDensity a) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}
	
	public MWNumericArray buildArray(int start, int count, int interval) {
		int[] dims = {1, count};
		MWNumericArray a = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
		int value = start;
		for (int index = 0; index < count; index++) {
			a.set(index+1, value);
			value += interval;
		}
		return a;
		
	}
	
	public MWNumericArray from(double[] d) {
		int[] dims = {1, d.length};
		MWNumericArray a = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
		for (int index = 0; index < d.length; index++) {
			a.set(index+1, d[index]);
		}
		return a;
	}
	
	public MWNumericArray from(double d) {
		return from(new double[] {d});
	}

	public DiscreteProbDensity gev(double k, double scale, double location) {
		DiscreteProbDensity result = new DiscreteProbDensity(maxTime/rangeinterval,0,maxTime,offset);
		int start = rangeinterval / 2;

		try {
			MWNumericArray mResult = (MWNumericArray)dist.dist_gevpdf(1, 
					buildArray(start,maxTime-rangeinterval+start, rangeinterval),
					from(k),
					from(scale),
					from(location))[0];
			result.setPdf(mResult.getDoubleData());
			mResult.dispose();
		} catch (MWException e) {
			e.printStackTrace();
			log.error(e.toString());
			return null;
		}

		return result.normalize();
	}

	public DiscreteProbDensity movingAverage(DiscreteProbDensity a, double span) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public DiscreteProbDensity multiDistribute(
			List<DiscreteProbDensity> pdfVector, List<Double> probVector) {
		DiscreteProbDensity initPdf = pdfVector.get(0);
		DiscreteProbDensity result = new DiscreteProbDensity(initPdf.numSlots,initPdf.min,initPdf.max,offset);
		result.raw = null;
		double sum = 0;
		for (int i=0; i<result.pdf.length;i++){
			for (int k=0;k<pdfVector.size();k++) {
				result.pdf[i] += probVector.get(k)*pdfVector.get(k).pdf[i];
			}
			sum += result.pdf[i];
		}
		if (sum <= 0) sum = 1;
		for (int i = 0; i < result.pdf.length; i++) {
			result.pdf[i] = result.pdf[i]/sum;
		}
		return result;
	}

	public DiscreteProbDensity newDiscreteProbDensity() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public DiscreteProbDensity norm(double mean, double std, double extra) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}
	
	public static void main(String[] args) {
		MathService service = new MathService();
		DiscreteProbDensity result = new DiscreteProbDensity(10,0,100,10);
		for (int i=0;i<result.pdf.length;i++) {
			result.pdf[i] = i;
		}
		result = service.shrink(result, 2, 0.5);
		for (double d:result.pdf) {
			System.out.print(d+" ");
		}
		//System.out.println(service.gev(2,1,1));
	}
}
