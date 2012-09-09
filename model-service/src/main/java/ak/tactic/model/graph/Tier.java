package ak.tactic.model.graph;


public class Tier {
	String name;
	
	String[] instances;
	Dependency[] dependencies;
	
	public Tier(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Tier) && ((Tier)obj).name.equals(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public String toString() {
		return name;
	}
}
