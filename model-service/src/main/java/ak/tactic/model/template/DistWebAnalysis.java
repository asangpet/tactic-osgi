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
public class DistWebAnalysis implements AnalysisInstance {

	protected Logger log = LoggerFactory.getLogger(DistWebAnalysis.class);
	
	@Autowired
	protected MathService matlab;
	
	Cluster cluster;
	Service service;
	AnalysisGraph graph;
	
	static final String NODE_ROOT = "10.43.1.1";
	static final String NODE_D1 = "10.43.1.2";
	static final String NODE_D2 = "10.43.1.3";
	
	public DistWebAnalysis() {
		cluster = new ImpactCluster("impact");
		((ImpactCluster)cluster).setLog(log);
		
		service = Builder.buildService("dist", NODE_ROOT)
					.dist(NODE_D1, NODE_D2).build();
		cluster.add(service).addHost("intelq5");
	}
	
	double findImpact(AnalysisGraph graph, Double relativeShift, String nodeName, String root) {
		Map<String, Double> relativeTransfer = new LinkedHashMap<String, Double>();
		relativeTransfer.put(nodeName, relativeShift);
		graph.predictTransfer(relativeTransfer);
		return graph.getNode(root).getAnalysisResponse().getPdf().mode();
	}
	
	void calculateImpact() {
		double origin = graph.getNode(NODE_ROOT).getAnalysisResponse().getPdf().mode();
		
		Map<Component, Double> impact = new LinkedHashMap<Component, Double>();
		for (Component comp:service.getComponents()) {
			impact.put(comp, findImpact(graph, 1d, comp.getName(), NODE_ROOT));
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
		densityMap.put(NODE_ROOT, matlab.gev(0.2, 100, 1200).setRawCount(100));
		densityMap.put(NODE_D1, matlab.gev(0.2, 100, 1100).setRawCount(100));
		densityMap.put(NODE_D2, matlab.gev(0.2, 100, 1100).setRawCount(100));
		graph.analyze(densityMap);
		
		Map<String, double[]> result = new LinkedHashMap<String, double[]>();
		
		result.put("oroot", graph.getNode(NODE_ROOT).getServerResponse().getPdf());
		result.put("od1", graph.getNode(NODE_D1).getServerResponse().getPdf());
		result.put("od2", graph.getNode(NODE_D2).getServerResponse().getPdf());		
		
		log.info("oroot - {}",graph.getNode(NODE_ROOT).getAnalysisResponse().getPdf().average());
		log.info("od1   - {}",graph.getNode(NODE_D1).getAnalysisResponse().getPdf().average());
		log.info("od2   - {}",graph.getNode(NODE_D2).getAnalysisResponse().getPdf().average());

		/* Manual shift
		Map<String, DiscreteProbDensity> transferMap = new LinkedHashMap<String, DiscreteProbDensity>();
		//transferMap.put("db", matlab.gaussian(60, 10).setRaw(500));
		transferMap.put("db", matlab.gev(0.2, 100, 300).setRaw(500));
		
		graph.predict(transferMap);
		*/
		
		/** Impact finder part
		findImpact(graph,1d,NODE_ROOT,NODE_ROOT);
		
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
		**/
		return result;
	}
	
	@Override
	public AnalysisGraph getAnalysisGraph() {
		return graph;
	}
}

