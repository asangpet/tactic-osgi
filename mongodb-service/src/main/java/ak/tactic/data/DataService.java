package ak.tactic.data;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.Mongo;

public class DataService {
	Mongo mongo;
	Jongo jongo;
	MongoCollection responseCollection;
	
	public DataService() throws Exception {
		mongo = new Mongo("127.0.0.1", 27017);
		jongo = new Jongo(mongo.getDB("test"));
		responseCollection = jongo.getCollection("response");
	}
	
	public Iterable<ResponseInfo> getResponses() {
		return responseCollection.find().as(ResponseInfo.class);
	}
	
	public void queryOne() {
		MongoCollection responses = jongo.getCollection("response");
		ResponseInfo resp = new ResponseInfo();
		SocketInfo serverSocket = new SocketInfo();
		serverSocket.setAddress("1.1.1.1");
		serverSocket.setPort(80);
		resp.setServer(serverSocket);
		
		SocketInfo clientSocket = new SocketInfo();
		clientSocket.setAddress("2.2.2.2");
		clientSocket.setPort(90);
		resp.setClient(clientSocket);
		
		resp.setRequest("HelloWorld");
		resp.setResponse("Goodbye");
		responses.save(resp);
	}
}
