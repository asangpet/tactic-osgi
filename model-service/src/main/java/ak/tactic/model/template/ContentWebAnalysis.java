package ak.tactic.model.template;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ak.tactic.math.DiscreteProbDensity;
import ak.tactic.math.MathService;
import ak.tactic.model.deployment.Cluster;
import ak.tactic.model.deployment.Component;
import ak.tactic.model.deployment.Host;
import ak.tactic.model.deployment.ImpactCluster;
import ak.tactic.model.deployment.Service;
import ak.tactic.model.deployment.VirtualMachine;
import ak.tactic.model.graph.AnalysisGraph;

@org.springframework.stereotype.Component
public class ContentWebAnalysis implements AnalysisInstance {
	protected Logger log = LoggerFactory.getLogger(DistWebAnalysis.class);
	
	@Autowired
	protected MathService matlab;
	
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	public ContentWebAnalysis() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		service = Builder.buildService("drupal", "varnish")
					.pushDist("bench-drupal")
						.dist("bench-memcache","bench-drupal-db","bench-solr")
					.pop()
					.pushDist("bench-nfs")
					.build();
		cluster.add(service).addHost("amdw6");
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
	
	public void setup() {
		graph = service.getAnalysisGraph();
	}
	
	@Override
	public Map<String, double[]> analyze() {
		Map<String, DiscreteProbDensity> densityMap = new LinkedHashMap<String, DiscreteProbDensity>();
		densityMap.put("varnish", matlab.gev(0.2, 100, 1200).setRawCount(100));
		densityMap.put("bench-drupal", matlab.gev(0.2, 100, 1100).setRawCount(100));
		densityMap.put("bench-drupal-db", matlab.gev(0.2, 100, 200).setRawCount(500));
		densityMap.put("bench-solr", matlab.gev(0.2, 100, 200).setRawCount(500));
		densityMap.put("bench-memcache", matlab.gev(0.2, 100, 200).setRawCount(500));
		graph.analyze(densityMap);
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		
		result.put("ovarnish", graph.getNode("varnish").getServerResponse().getPdf());
		result.put("odrupal", graph.getNode("bench-drupal").getServerResponse().getPdf());
		result.put("odb", graph.getNode("bench-drupal-db").getServerResponse().getPdf());		
		result.put("osolr", graph.getNode("bench-solr").getServerResponse().getPdf());		
		result.put("ocache", graph.getNode("bench-memcache").getServerResponse().getPdf());		
		
		log.info("ovar - {}",graph.getNode("varnish").getAnalysisResponse().getPdf().average());
		log.info("odrup- {}",graph.getNode("bench-drupal").getAnalysisResponse().getPdf().average());
		log.info("odb  - {}",graph.getNode("bench-drupal-db").getAnalysisResponse().getPdf().average());
		log.info("osolr- {}",graph.getNode("bench-solr").getAnalysisResponse().getPdf().average());
		log.info("ocache- {}",graph.getNode("bench-memcache").getAnalysisResponse().getPdf().average());

		/* Manual shift
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
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
		
		return result;
	}
	
	@Override
	public AnalysisGraph getAnalysisGraph() {
		return graph;
	}
}
