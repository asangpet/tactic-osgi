package ak.tactic.model.simulator.event;

public class RequestCompleteEvent {
	long processingTime;
	long startTime;
	long finishTime;
	
	public long getFinishTime() {
		return finishTime;
	}
	
	public long getProcessingTime() {
		return processingTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public RequestCompleteEvent setFinishTime(long finishTime) {
		this.finishTime = finishTime;
		return this;
	}
	
	public RequestCompleteEvent setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
		return this;
	}
	
	public RequestCompleteEvent setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append("Request finish in ")
			.append(finishTime-startTime)
			.append("ms (s:")
			.append(startTime)
			.append(" f:")
			.append(finishTime)
			.append(")")
			.toString();
	}
}
