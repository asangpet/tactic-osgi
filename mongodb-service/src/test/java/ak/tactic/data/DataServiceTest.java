package ak.tactic.data;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataServiceTest {
	Logger log = LoggerFactory.getLogger(DataServiceTest.class); 
	static DataService dataService;
	
	@BeforeClass
	public static void setup() throws Exception {
		dataService = new DataService();		
	}
	
	@Test
	public void testRun() {
		dataService.queryOne();
	}
	
	@Test
	public void testList() {
		Iterable<ResponseInfo> infos = dataService.getResponses("responseTime");
		for (ResponseInfo info : infos) {
			System.out.println(""+info);
		}
	}
	
}
