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

	public DiscreteProbDensity deconvreg(DiscreteProbDensity a,DiscreteProbDensity b) {
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
		System.out.println(service.gev(2,1,1));
	}
}
