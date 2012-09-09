package ak.tactic.model.deployment;

import java.util.ArrayList;
import java.util.List;

public class Host extends Entity {
	List<VirtualMachine> tenants;
	double load;
	
	public Host(String name) {
		super(name);
		tenants = new ArrayList<VirtualMachine>();
	}
	
	public void reset() {
		load = 0;
		tenants = new ArrayList<VirtualMachine>();
	}
	
	public void add(VirtualMachine vm) {
		tenants.add(vm);
	}
}
