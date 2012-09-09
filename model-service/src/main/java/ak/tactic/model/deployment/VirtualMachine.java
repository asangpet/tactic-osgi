package ak.tactic.model.deployment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A virtual machine can host multiple components
 * 
 * @author asangpet
 *
 */
public class VirtualMachine extends Entity {
	Map<String,Component> tenants;
	double score;
	
	public VirtualMachine(String name) {
		super(name);
		tenants = new LinkedHashMap<String, Component>();
	}
	
	public VirtualMachine add(Component component) {
		tenants.put(component.getName(),component);
		return this;
	}
	
	public Collection<Component> getTenants() {
		return tenants.values();
	}
}
