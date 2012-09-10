package ak.tactic.model.data;

import org.bson.types.ObjectId;

import ak.tactic.model.math.DiscreteProbDensity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionData {
	@JsonProperty("_id")
	private ObjectId id;
	
	private String name;
	private String model;
	private double[] data;
	
	public DistributionData() {
		data = new double[] {};
	}
	
	public DistributionData(String model, String name, DiscreteProbDensity prob) {
		this.model = model;
		this.name = name;
		this.data = prob.getPdf();
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setData(double[] data) {
		this.data = data;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public double[] getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
	
	public String getModel() {
		return model;
	}
	public ObjectId getId() {
		return id;
	}
}
