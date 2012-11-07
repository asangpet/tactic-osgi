package ak.tactic.model.simulator.event;

import ak.tactic.model.simulator.Worker;

public class RequestCompleteEvent implements Comparable<RequestCompleteEvent> {
	long processingTime;
	long requestTime;
	long startTime;
	long finishTime;
	Worker runner;
	
	RequestArrivalEvent origin;
	
	public long getFinishTime() {
		return finishTime;
	}
	
	public long getProcessingTime() {
		return processingTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getRequestTime() {
		return requestTime;
	}
	
	public Worker getRunner() {
		return runner;
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
	
	public RequestCompleteEvent setRequestTime(long requestTime) {
		this.requestTime = requestTime;
		return this;
	}
	
	public RequestCompleteEvent setRunner(Worker runner) {
		this.runner = runner;
		return this;
	}
	
	public RequestCompleteEvent setOrigin(RequestArrivalEvent origin) {
		this.origin = origin;
		return this;
	}
	
	public RequestArrivalEvent getOrigin() {
		return origin;
	}
	
	@Override
	public int compareTo(RequestCompleteEvent other) {
		if (this.equals(other)) {
			return 0;
		} else {
			if (requestTime - other.requestTime < 0) {
				return -1;
			} else if (requestTime - other.requestTime > 0) {
				return 1;
			} else {
				return this.hashCode() - other.hashCode();
			}
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append("Request finish in ")
			.append(finishTime-requestTime)
			.append("ms (p:")
			.append(finishTime-startTime)
			.append(" q:")
			.append(startTime-requestTime)
			.append(" on ")
			.append(runner.getRunnerId())
			.append(")")
			.toString();
	}
}
