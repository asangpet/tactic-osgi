package ak.tactic.data;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.Mongo;

public class DataService {
	Mongo mongo;
	Jongo jongo;
	MongoCollection responseCollection;
	
	static final String modelCollection = "model";
	static final String coarrivalCollection = "coarrival";
	
	public DataService() throws Exception {
		/*
		mongo = new Mongo("127.0.0.1", 27017);
		jongo = new Jongo(mongo.getDB("collector_b"));
		*/
		mongo = new Mongo("10.1.4.7", 27017);
		jongo = new Jongo(mongo.getDB("mongoTime"));
		responseCollection = jongo.getCollection("responseTime");
		getModelCollection().ensureIndex("{name:1}");
	}
	
	public MongoCollection getCollection(String collectionName) {
		return jongo.getCollection(collectionName);
	}
	
	public MongoCollection getModelCollection() {
		return jongo.getCollection(modelCollection);
	}
	
	public MongoCollection getCoarrivalCollection() {
		return jongo.getCollection(coarrivalCollection);
	}
	
	public Iterable<ResponseInfo> getResponses(String collectionName) {
		final MongoCollection collection = jongo.getCollection(collectionName);
		return collection.find().as(ResponseInfo.class);
	}
	
	public Iterable<ResponseInfo> getResponsesByTime(String collectionName) {
		final MongoCollection collection = jongo.getCollection(collectionName);
		return collection.find("{}").sort("{timestamp:1}").as(ResponseInfo.class);
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
