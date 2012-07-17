package ak.tactic.data;

public class SocketInfo {
	private String address;
	private int port;
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public String toString() {
		return "address:"+address+",port:"+port;
	}
}
