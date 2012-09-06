package ak.tactic.model.graph;

import java.util.List;

import ak.tactic.math.DiscreteProbDensity;

public class TransferFunction {
	public DiscreteProbDensity inputPdf;
	public DiscreteProbDensity outputPdf;
	
	public DiscreteProbDensity nonparamPdf;
	public DiscreteProbDensity editedNonparamPdf;
	
	public DiscreteProbDensity pdf;
	
	public double[] param;
	
	public List<double[]> linkparam;
	
	public TransferFunction(double[] param, DiscreteProbDensity nonparamPdf) {
		this.param = param;
		this.nonparamPdf = nonparamPdf;
	}
	
	public TransferFunction(double[] param) {
		this(param,null);
	}
	
	public TransferFunction(double[] param, List<double[]> linkparam, DiscreteProbDensity nonparamPdf) {
		this.param = param;
		this.linkparam = linkparam;
		this.nonparamPdf = nonparamPdf;
	}
	
	public DiscreteProbDensity getPdf() {
		return pdf;
	}
	
	public void setPdf(DiscreteProbDensity pdf) {
		this.pdf = pdf;
	}
	
	public DiscreteProbDensity getNonparamPdf() {
		return nonparamPdf;
	}
	
	public DiscreteProbDensity getInputPdf() {
		return inputPdf;
	}
	
	public DiscreteProbDensity getOutputPdf() {
		return outputPdf;
	}
	
	public void setEditedNonparamPdf(DiscreteProbDensity editedNonparamPdf) {
		this.editedNonparamPdf = editedNonparamPdf;
	}
	
	public void setInputPdf(DiscreteProbDensity inputPdf) {
		this.inputPdf = inputPdf;
	}
	
	public void setOutputPdf(DiscreteProbDensity outputPdf) {
		this.outputPdf = outputPdf;
	}
	
	public void setNonparamPdf(DiscreteProbDensity nonparamPdf) {
		this.nonparamPdf = nonparamPdf;
	}
	
	public void setLinkparam(List<double[]> linkparam) {
		this.linkparam = linkparam;
	}
}
