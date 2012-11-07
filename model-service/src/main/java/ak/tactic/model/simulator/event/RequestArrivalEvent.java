package ak.tactic.model.simulator.event;

public class RequestArrivalEvent {
	public static final long DEFAULT_START_TIME = -1;
	
	long timestamp;
	long processingTime;
	long processedTime = 0;
	long startTime = DEFAULT_START_TIME;
	int targetRunner;
	
	public RequestArrivalEvent(long timestamp, long processingTime, int targetRunner) {
		this.timestamp = timestamp;
		this.processingTime = processingTime;
		this.targetRunner = targetRunner;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append("{ Request Arrival time:").append(timestamp)
			.append(" proc:").append(processingTime)
			.append(" start:").append(startTime)
			.append(" }")
			.toString();
	}
	
	public void process(long time) {
		processedTime += time;
	}
	
	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setTargetRunner(int targetRunner) {
		this.targetRunner = targetRunner;
	}
	
	public int getTargetRunner() {
		return targetRunner;
	}
	
	public long getProcessingTime() {
		return processingTime;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getProcessedTime() {
		return processedTime;
	}
}
