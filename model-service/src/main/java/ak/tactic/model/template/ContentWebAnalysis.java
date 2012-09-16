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
public class ContentWebAnalysis implements AnalysisInstance {
	protected Logger log = LoggerFactory.getLogger(DistWebAnalysis.class);
	
	@Autowired
	protected MathService matlab;
	
	@Autowired
	ModelDataService modelData;
	
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	static final String PROFILE_MODEL = "multicore_profile";
	static final String ACTUAL_MODEL  = "responseTime";
	
	static final String VARNISH  = "10.0.50.2";
	static final String NFS      = "10.0.91.1";
	static final String DRUPAL1  = "10.0.60.1";
	static final String DRUPAL2  = "10.0.60.2";
	static final String DRUPAL3  = "10.0.60.3";
	static final String MYSQL    = "10.0.70.1";
	static final String SOLR     = "10.0.80.1";
	static final String MEMCACHE = "10.0.90.1";
	
	@PostConstruct
	public void init() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		service = Builder.buildService("drupal", VARNISH)
					.pushDist(DRUPAL1)
						.dist(MYSQL, SOLR, MEMCACHE)
					.pop()
					.pushDist(DRUPAL2)
						.dist(MYSQL, SOLR, MEMCACHE)
					.pop()
					.pushDist(DRUPAL3)
						.dist(MYSQL, SOLR, MEMCACHE)
					.pop()
					.pushDist(NFS)
					.build();
		cluster.add(service).addHost("intelq5");
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
		double origin = graph.getNode("varnish").getAnalysisResponse().getPdf().mode();
		
		Map<Component, Double> impact = new LinkedHashMap<Component, Double>();
		for (Component comp:service.getComponents()) {
			impact.put(comp, findImpact(graph, 1d, comp.getName(), "varnish"));
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
		densityMap.put(VARNISH, modelData.getPdf(PROFILE_MODEL, VARNISH));
		densityMap.put(NFS, modelData.getPdf(PROFILE_MODEL, NFS));
		densityMap.put(DRUPAL1, modelData.getPdf(PROFILE_MODEL, DRUPAL1));
		densityMap.put(DRUPAL2, modelData.getPdf(PROFILE_MODEL, DRUPAL2));
		densityMap.put(DRUPAL3, modelData.getPdf(PROFILE_MODEL, DRUPAL3));
		densityMap.put(MYSQL, modelData.getPdf(PROFILE_MODEL, MYSQL));
		densityMap.put(SOLR, modelData.getPdf(PROFILE_MODEL, SOLR));
		densityMap.put(MEMCACHE, modelData.getPdf(PROFILE_MODEL, MEMCACHE));
		graph.analyze(densityMap);
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		
		result.put("ovarnish", graph.getNode(VARNISH).getServerResponse().getPdf());
		result.put("onfs", graph.getNode(NFS).getServerResponse().getPdf());
		result.put("odrupal1", graph.getNode(DRUPAL1).getServerResponse().getPdf());
		result.put("odrupal2", graph.getNode(DRUPAL2).getServerResponse().getPdf());
		result.put("odrupal3", graph.getNode(DRUPAL3).getServerResponse().getPdf());
		result.put("omysql", graph.getNode(MYSQL).getServerResponse().getPdf());		
		result.put("osolr", graph.getNode(SOLR).getServerResponse().getPdf());		
		result.put("ocache", graph.getNode(MEMCACHE).getServerResponse().getPdf());		
		
		result.put("varnish", graph.getNode(VARNISH).getAnalysisResponse().getPdf().getPdf());
		result.put("nfs", graph.getNode(NFS).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal1", graph.getNode(DRUPAL1).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal2", graph.getNode(DRUPAL2).getAnalysisResponse().getPdf().getPdf());
		result.put("drupal3", graph.getNode(DRUPAL3).getAnalysisResponse().getPdf().getPdf());
		result.put("mysql", graph.getNode(MYSQL).getAnalysisResponse().getPdf().getPdf());		
		result.put("solr", graph.getNode(SOLR).getAnalysisResponse().getPdf().getPdf());		
		result.put("cache", graph.getNode(MEMCACHE).getAnalysisResponse().getPdf().getPdf());
		
		// Added prediction from actual result		
		Map<String, DiscreteProbDensity> actualPdf = new LinkedHashMap<String, DiscreteProbDensity>();
		actualPdf.put(NFS, modelData.getPdf(ACTUAL_MODEL, NFS));
		actualPdf.put(MYSQL, modelData.getPdf(ACTUAL_MODEL, MYSQL));
		actualPdf.put(SOLR, modelData.getPdf(ACTUAL_MODEL, SOLR));
		actualPdf.put(MEMCACHE, modelData.getPdf(ACTUAL_MODEL, MEMCACHE));
		graph.predict(actualPdf);
		
		result.put("pvarnish", graph.getNode(VARNISH).getAnalysisResponse().getPdf().getPdf());
		result.put("pnfs", graph.getNode(NFS).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal1", graph.getNode(DRUPAL1).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal2", graph.getNode(DRUPAL2).getAnalysisResponse().getPdf().getPdf());
		result.put("pdrupal3", graph.getNode(DRUPAL3).getAnalysisResponse().getPdf().getPdf());
		result.put("pmysql", graph.getNode(MYSQL).getAnalysisResponse().getPdf().getPdf());		
		result.put("psolr", graph.getNode(SOLR).getAnalysisResponse().getPdf().getPdf());		
		result.put("pcache", graph.getNode(MEMCACHE).getAnalysisResponse().getPdf().getPdf());
		
		result.put("avarnish", modelData.getPdf(ACTUAL_MODEL, VARNISH).getPdf());
		result.put("adrupal1", modelData.getPdf(ACTUAL_MODEL, DRUPAL1).getPdf());
		result.put("adrupal2", modelData.getPdf(ACTUAL_MODEL, DRUPAL2).getPdf());
		result.put("adrupal3", modelData.getPdf(ACTUAL_MODEL, DRUPAL3).getPdf());
		
		//Find percentile
		double[] qarray = new double[100];
		for (int i=0;i<100;i++) {
			qarray[i] = i/100.0;
		}
		result.put("prctile_predict",graph.getNode(VARNISH).getAnalysisResponse().getPdf().getQuantile(qarray));
		result.put("prctile_actual",modelData.getPdf(ACTUAL_MODEL, VARNISH).getQuantile(qarray));
		result.put("prctile_source",modelData.getPdf(PROFILE_MODEL, VARNISH).getQuantile(qarray));

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
