package ak.tactic.model.simulator.event;

public class RequestArrivalEvent {
	long timestamp;
	long processingTime;
	
	public RequestArrivalEvent(long timestamp, long processingTime) {
		this.timestamp = timestamp;
		this.processingTime = processingTime;
	}
	
	@Override
	public String toString() {
		return "{ Request Arrival time:"+timestamp+" proc:"+processingTime+" }";
	}
	
	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getProcessingTime() {
		return processingTime;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
