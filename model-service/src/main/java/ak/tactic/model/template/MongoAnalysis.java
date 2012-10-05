package ak.tactic.model.template;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ak.tactic.model.App;
import ak.tactic.model.ModelDataService;
import ak.tactic.model.deployment.Cluster;
import ak.tactic.model.deployment.Component;
import ak.tactic.model.deployment.Host;
import ak.tactic.model.deployment.ImpactCluster;
import ak.tactic.model.deployment.Service;
import ak.tactic.model.deployment.VirtualMachine;
import ak.tactic.model.graph.AnalysisGraph;
import ak.tactic.model.math.DiscreteProbDensity;
import ak.tactic.model.math.MathService;

@org.springframework.stereotype.Component
public class MongoAnalysis implements AnalysisInstance {
	protected Logger log = LoggerFactory.getLogger(MongoAnalysis.class);
	
	@Autowired
	protected MathService matlab;
	
	@Autowired
	ModelDataService modelData;
	
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	static final String PROFILE_MODEL = "mongoTime";
	static final String ACTUAL_MODEL  = "mongoTime";
	
	static final String MONGOS   = "10.4.3.1";
	static final String CONFIG1  = "10.4.1.1";
	static final String CONFIG2  = "10.4.1.2";
	static final String CONFIG3  = "10.4.1.3";
	static final String MONGOD1  = "10.4.2.1";
	static final String MONGOD2  = "10.4.2.2";
	static final String MONGOD3  = "10.4.2.3";
	static final String MONGOD4  = "10.4.2.4";
	
	@PostConstruct
	public void init() {
		cluster = new ImpactCluster("mongoimpact");
		((ImpactCluster)cluster).setLog(log);
		service = Builder.buildService("mongo", MONGOS)
				.pushComp(CONFIG1, CONFIG2, CONFIG3)
				.pop()
				.pushDist(MONGOD1, MONGOD2, MONGOD3, MONGOD4)
				.build();
		cluster.add(service).addHost("fx2");
		graph = service.getAnalysisGraph();
		graph.setMatlab(matlab);
	}
	
	public void setApp(App app) {
		graph.setApp(app);
	}
	
	double findImpact(AnalysisGraph graph, Double relativeShift, String nodeName, String root) {
		Map<String, Double> relativeTransfer = new LinkedHashMap<String, Double>();
		relativeTransfer.put(nodeName, relativeShift);
		graph.predictTransfer(relativeTransfer);
		return graph.getNode(root).getAnalysisResponse().getPdf().mode();
	}
	
	void calculateImpact() {
		double origin = graph.getNode(MONGOS).getAnalysisResponse().getPdf().mode();
		
		Map<Component, Double> impact = new LinkedHashMap<Component, Double>();
		for (Component comp:service.getComponents()) {
			impact.put(comp, findImpact(graph, 1d, comp.getName(), MONGOS));
		}
		
		log.info("    origin - {}",origin);
		for (Component comp:impact.keySet()) {
			log.info("{} impact - {}",comp.getName(), impact.get(comp));
			comp.setImpact((impact.get(comp)-origin)/origin);
		}
	}
	
	@Override
	public Map<Host,Collection<VirtualMachine>> calculatePlacement() {
		/*
		for (int id=0;id<10;id++) {
			setup(""+id);
			calculateImpact(""+id);
		}
		*/
		for (int i=0;i<1;i++) {
			log.debug("Place iteration {}",i);
			((ImpactCluster)cluster).place();
			Map<Service,Double> impact = cluster.evaluate();
		
			for (Service svc:impact.keySet()) {
				log.info("Impact {} = {}",svc.getName(), impact.get(svc));
			}
		}
		return cluster.getMapping().asMap();
	}
	
	@Override
	public Map<String, double[]> analyze() {
		Map<String, DiscreteProbDensity> densityMap = new LinkedHashMap<String, DiscreteProbDensity>();
		densityMap.put(MONGOS, modelData.getPdf(PROFILE_MODEL, MONGOS));
		densityMap.put(CONFIG1, modelData.getPdf(PROFILE_MODEL, CONFIG1));
		densityMap.put(CONFIG2, modelData.getPdf(PROFILE_MODEL, CONFIG2));
		densityMap.put(CONFIG3, modelData.getPdf(PROFILE_MODEL, CONFIG3));
		densityMap.put(MONGOD1, modelData.getPdf(PROFILE_MODEL, MONGOD1));
		densityMap.put(MONGOD2, modelData.getPdf(PROFILE_MODEL, MONGOD2));
		densityMap.put(MONGOD3, modelData.getPdf(PROFILE_MODEL, MONGOD3));
		densityMap.put(MONGOD4, modelData.getPdf(PROFILE_MODEL, MONGOD4));
		graph.analyze(densityMap);
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		
		result.put("ovarnish", graph.getNode(MONGOS).getServerResponse().getPdf());
		result.put("onfs", graph.getNode(CONFIG1).getServerResponse().getPdf());
		result.put("odrupal1", graph.getNode(CONFIG2).getServerResponse().getPdf());
		result.put("odrupal2", graph.getNode(CONFIG3).getServerResponse().getPdf());
		result.put("odrupal3", graph.getNode(MONGOD1).getServerResponse().getPdf());
		result.put("omysql", graph.getNode(MONGOD2).getServerResponse().getPdf());		
		result.put("osolr", graph.getNode(MONGOD3).getServerResponse().getPdf());		
		result.put("ocache", graph.getNode(MONGOD4).getServerResponse().getPdf());		
		
		result.put("varnish", graph.getNode(MONGOS).getAnalysisResponse().getPdf().getPdf());
		result.put("nfs", graph.getNode(CONFIG1).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal1", graph.getNode(CONFIG2).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal2", graph.getNode(CONFIG3).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal3", graph.getNode(MONGOD1).getAnalysisResponse().getPdf().getPdf());
		result.put("mysql", graph.getNode(MONGOD2).getAnalysisResponse().getPdf().getPdf());		
		result.put("solr", graph.getNode(MONGOD3).getAnalysisResponse().getPdf().getPdf());		
		result.put("cache", graph.getNode(MONGOD4).getAnalysisResponse().getPdf().getPdf());
		
		// Added prediction from actual result		
		Map<String, DiscreteProbDensity> actualPdf = new LinkedHashMap<String, DiscreteProbDensity>();
		actualPdf.put(CONFIG1, modelData.getPdf(ACTUAL_MODEL, CONFIG1));
		actualPdf.put(MONGOD2, modelData.getPdf(ACTUAL_MODEL, MONGOD2));
		actualPdf.put(MONGOD3, modelData.getPdf(ACTUAL_MODEL, MONGOD3));
		actualPdf.put(MONGOD4, modelData.getPdf(ACTUAL_MODEL, MONGOD4));
		graph.predict(actualPdf);
		
		result.put("pvarnish", graph.getNode(MONGOS).getAnalysisResponse().getPdf().getPdf());
		result.put("pnfs", graph.getNode(CONFIG1).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal1", graph.getNode(CONFIG2).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal2", graph.getNode(CONFIG3).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal3", graph.getNode(MONGOD1).getAnalysisResponse().getPdf().getPdf());
		result.put("pmysql", graph.getNode(MONGOD2).getAnalysisResponse().getPdf().getPdf());		
		result.put("psolr", graph.getNode(MONGOD3).getAnalysisResponse().getPdf().getPdf());		
		result.put("pcache", graph.getNode(MONGOD4).getAnalysisResponse().getPdf().getPdf());
		
		result.put("avarnish", modelData.getPdf(ACTUAL_MODEL, MONGOS).getPdf());
		result.put("adrupal1", modelData.getPdf(ACTUAL_MODEL, CONFIG2).getPdf());
		result.put("adrupal2", modelData.getPdf(ACTUAL_MODEL, CONFIG3).getPdf());
		result.put("adrupal3", modelData.getPdf(ACTUAL_MODEL, MONGOD1).getPdf());
		result.put("amysql", modelData.getPdf(ACTUAL_MODEL, MONGOD2).getPdf());
		result.put("asolr", modelData.getPdf(ACTUAL_MODEL, MONGOD3).getPdf());
		result.put("acache", modelData.getPdf(ACTUAL_MODEL, MONGOD4).getPdf());
		
		//Find percentile
		double[] qarray = new double[100];
		for (int i=0;i<100;i++) {
			qarray[i] = i/100.0;
		}
		result.put("prctile_predict",graph.getNode(MONGOS).getAnalysisResponse().getPdf().getQuantile(qarray));
		result.put("prctile_actual",modelData.getPdf(ACTUAL_MODEL, MONGOS).getQuantile(qarray));
		result.put("prctile_source",modelData.getPdf(PROFILE_MODEL, MONGOS).getQuantile(qarray));

		/* Manual shift
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
		/*
		findImpact(graph,1d,"varnish","varnish");
		
		log.info("var - {}",graph.getNode("varnish").getAnalysisResponse().getPdf().average());
		log.info("drup- {}",graph.getNode("bench-drupal").getAnalysisResponse().getPdf().average());
		log.info("db  - {}",graph.getNode("bench-drupal-db").getAnalysisResponse().getPdf().average());
		log.info("solr- {}",graph.getNode("bench-solr").getAnalysisResponse().getPdf().average());
		log.info("cache-{}",graph.getNode("bench-memcache").getAnalysisResponse().getPdf().average());
		
		result.put("varnish", graph.getNode("varnish").getAnalysisResponse().getPdf().getPdf());
		result.put("drupal", graph.getNode("bench-drupal").getAnalysisResponse().getPdf().getPdf());
		result.put("db", graph.getNode("bench-drupal-db").getAnalysisResponse().getPdf().getPdf());
		result.put("solr", graph.getNode("bench-solr").getAnalysisResponse().getPdf().getPdf());
		result.put("cache", graph.getNode("bench-memcache").getAnalysisResponse().getPdf().getPdf());
		*/
		return result;
	}
	
	@Override
	public AnalysisGraph getAnalysisGraph() {
		return graph;
	}
}
