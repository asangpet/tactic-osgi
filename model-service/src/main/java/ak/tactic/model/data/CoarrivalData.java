package ak.tactic.model.data;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoarrivalData {
	@JsonProperty("_id")
	private ObjectId id;

	@JsonProperty("m")
	private String model;
	@JsonProperty("a")
	private String referencedComponent;
	@JsonProperty("b")
	private String interferingComponent;
	@JsonProperty("c")
	private double coarrival;
	
	public CoarrivalData() {
	}
	
	public CoarrivalData(String model, String ref, String inf, double coarrival) {
		this.referencedComponent = ref;
		this.interferingComponent = inf;
		this.coarrival = coarrival;
		this.model = model;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getReferencedComponent() {
		return referencedComponent;
	}

	public void setReferencedComponent(String referencedComponent) {
		this.referencedComponent = referencedComponent;
	}

	public String getInterferingComponent() {
		return interferingComponent;
	}

	public void setInterferingComponent(String interferingComponent) {
		this.interferingComponent = interferingComponent;
	}
	
	public double getCoarrival() {
		return coarrival;
	}
	
	public void setCoarrival(double coarrival) {
		this.coarrival = coarrival;
	}
}
