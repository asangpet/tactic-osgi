package ak.tactic.model.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ak.tactic.model.App;
import ak.tactic.model.math.DiscreteProbDensity;
import ak.tactic.model.math.MathService;
import ak.tactic.model.math.ParametricDensity;

public class AnalysisGraph extends InstanceGraph {
	Node root = null;
	double EXPANSION_FACTOR = 1;
	boolean FIND_PARAMETRIC = false;
	boolean USE_EXPANSION = true;

	MathService matlab;
	App app;
	double rawDistThreshold = 0.2;
	
	Logger log = LoggerFactory.getLogger(AnalysisGraph.class);
	
	public void setApp(App app) {
		this.app = app;
	}
	
	public void setMatlab(MathService matlab) {
		this.matlab = matlab;
	}

	DiscreteProbDensity getPdf(double shape,double scale,double location) {
		return matlab.gev(shape,scale,location);
	}
		
	Double getCoarrival(Node a, Node b) {
		return app.getCoarrivalMatrix().get(a.getName()+"|"+b.getName());
	}
	
	double getExpectedCoarrival(Node a) {
		int count = 0;
		double sum = 0;
		for (Node node : getNodeList()) {
			if (node.getName().equals(a.getName())) {
				continue;
			}
			Double co = getCoarrival(a,node);
			if (!co.isNaN()) {
				count++;
				sum+=co;
			}
		}
		return sum / count;
	}
	

	public void analyze(Map<String,DiscreteProbDensity> densityMap) {
		resetMark();

		if (root == null) root = nodeList.get(0);
		for (Node it:nodeList) {
			it.analysisResponse = null;
			it.model = null;
		}
		for (Map.Entry<String, DiscreteProbDensity> item:densityMap.entrySet()) {
			if (nodes.get(item.getKey()) != null) {
				nodes.get(item.getKey()).serverResponse = item.getValue();
			}
		}

		// calculate average tier request count (need for multiple convolution at heavily used tier)
		for (Node node:nodeList) {
			if (node.serverResponse != null) {
				int uptierCount = 0;
				if (node.parents != null) {

					for (Node parent:node.parents.nodes) {
						if (parent.serverResponse != null)
							uptierCount += parent.serverResponse.getRawCount();
					}
					if (uptierCount > 0) node.requestCount = Math.round(node.serverResponse.getRawCount() / uptierCount);
					if (node.requestCount < 1) node.requestCount = 1;
					log.debug("------Estimating tier request count {} - {}",node,node.requestCount);
				}
			}
		}

		analyzeResponse(root);
		predict(new LinkedHashMap<String, DiscreteProbDensity>());
		//predict(densityMap);
	}

	/**
	 * Predict analysis response based on the given server response of the specified node.
	 * 
	 * @param densityMap
	 */
	public void predict(Map<String, DiscreteProbDensity> densityMap) {
		resetMark();

		if (root == null) root = nodeList.get(0);

		for (Node it:nodeList) {
			it.analysisResponse = null;
		}
		for (Map.Entry<String, DiscreteProbDensity> entry:densityMap.entrySet()) {
			Node nodeEntry = nodes.get(entry.getKey());
			nodeEntry.serverResponse = entry.getValue();
			nodeEntry.edited = true;
		}
		predictResponse(root);
	}

	public void predictTransfer(Map<String, Double> transferMap) {
		resetMark();

		if (root == null) root = nodeList.get(0);
		for (Node it:nodeList) {
			it.analysisResponse = null;
		}

		for (Map.Entry<String, Double> entry: transferMap.entrySet()) {
			Node nodeEntry = nodes.get(entry.getKey());

			nodeEntry.shiftValue = entry.getValue();
			nodeEntry.transferEdited = true;
			if ((nodeEntry.model!=null) && (nodeEntry.model.getTransfer()!=null) && (nodeEntry.model.getTransfer().getNonparamPdf() != null)) {
				double ranges = Math.abs(nodeEntry.model.getTransfer().getOutputPdf().average()-nodeEntry.model.transfer.getInputPdf().average());
				nodeEntry.model.transfer.setEditedNonparamPdf(
						nodeEntry.model.getTransfer().getNonparamPdf().shiftByValue(entry.getValue()*ranges));
			}
		}
		predictResponse(root);
	}

	double setCutoff(DiscreteProbDensity pdf) {
		return pdf.average()+10*pdf.stdev();
	}

	void analyzeResponse(Node node) {
		Subgraph children = getChildren(node);
		Set<Link> links = children.links;
		Set<Node> childs = children.nodes;

		if (node.mark) {
			// We've already considered this node
			return;
		}

		if (childs.size() == 0) {
			// terminal case, should fit a distribution
			if (node.serverResponse != null) {
				if (node.model == null) {
					// use fit result as the model
					node.model = new NodeModel(new DiscreteProbDensity(node.serverResponse),
							new double[] {}, node.getServerResponse().getRawCount());
					node.model.cutoff = setCutoff(node.serverResponse);
					log.debug("{} - {} - {}",new Object[] {node,node.serverResponse.average(),node.model.param});

					node.analysisResponse = new ParametricDensity(node.serverResponse);
					node.analysisResponse.getPdf().setRawCount(node.serverResponse.getRawCount());
					node.modelpdf = new DiscreteProbDensity(node.serverResponse);

					log.debug("{} analyzed - {}",node,node.analysisResponse);
					node.mark = true;

				} else {
					log.debug("**Node {} model have already been generated", node);
				}
			} else {
				log.debug("No-information node during model creation (should be cached node only) - {}",node);
			}
		} else {
			// first we try to analyze all the children
			for (Node it:childs) {
				analyzeResponse(it);
			}

			List<ParametricDensity> compResp = new ArrayList<ParametricDensity>();
			List<ParametricDensity> distResp = new LinkedList<ParametricDensity>();
			List<Double> distProb = new LinkedList<Double>();
			List<Integer> requestCounter = new ArrayList<Integer>();
			double totalRawCount = 0;
			double existingDistProb = 0.0;
			List<Link> distLinks = new LinkedList<Link>();
			List<Link> unknownLink = new LinkedList<Link>();
			
			// then we attempt to combine the result based on link type
			for (Link link:links) {
				if (link.target.analysisResponse != null) {
					if (link.type instanceof CompositionDependency) {
						for (int i=0;i<link.target.requestCount;i++) {
							compResp.add(link.target.analysisResponse);
							requestCounter.add(link.target.requestCount);
						}
					} else if (link.type instanceof DistributionDependency) {
						distLinks.add(link);
						distResp.add(link.target.analysisResponse);
						if (link.type.distProb == null) {
							// Adjust distribution probability based on traffic here
							totalRawCount += link.target.getAnalysisResponse().getPdf().getRawCount();
							log.debug("Request count on {} (parent {}) =${}/{}", new Object[] {link.target,node,link.target.analysisResponse.getPdf().getRawCount(),totalRawCount});
							//link.type.distProb = (double)link.target.analysisResponse.pdf.rawCount/node.serverResponse.rawCount
						} else {
							existingDistProb += link.type.distProb;
						}
						//distProb << link.type.distProb
					}
				} else {
					unknownLink.add(link);
				}
			}

			// rebalance distribution link probability
			if (existingDistProb < 1.0) {
				totalRawCount = totalRawCount / (1.0-existingDistProb);
				if ((node.serverResponse!=null) && (node.serverResponse.getRawCount() != null)) {
					// compare estimated total with the actual response on the server
					// use the maximum
					totalRawCount = Math.max(totalRawCount, node.serverResponse.getRawCount());
				}
				for (Link link:distLinks) {
					if (link.type.distProb == null) {
						link.type.distProb = link.target.analysisResponse.getPdf().getRawCount() / totalRawCount;
						log.debug("Assigned distribution prob for {} to {}",link,link.type.distProb);
					}
					distProb.add(link.type.distProb);
				}
			} else {
				for (Link l:distLinks) {
					distProb.add(l.type.distProb);
				}
			}

			// first we convolve all the composite response
			DiscreteProbDensity compositeRespPdf = null;
			if (compResp.size()>0) {
				compositeRespPdf = compResp.get(0).getPdf();
				for (int i=1;i<compResp.size();i++) {
					compositeRespPdf = compositeRespPdf.tconv(compResp.get(i).getPdf());
					if (compositeRespPdf.getRawCount() == null) compositeRespPdf.setRawCount(0L);
					compositeRespPdf.setRawCount(compositeRespPdf.getRawCount() + compResp.get(i).getPdf().getRawCount() / requestCounter.get(i));
				}
				log.debug("Composite analyzed avg {}, compResp {}",compositeRespPdf.average(), compResp.size());
			}

			// next we calculate the response from distribution link
			DiscreteProbDensity distRespPdf = null;
			if (distResp.size() > 0) {
				List<DiscreteProbDensity> dPdf = new ArrayList<DiscreteProbDensity>();
				for (ParametricDensity it:distResp) {
					dPdf.add(it.getPdf());
				}
				distRespPdf = matlab.multiDistribute(dPdf,distProb);
				distRespPdf.setRawCount(0);
				for (ParametricDensity it:distResp) {
					distRespPdf.setRawCount(distRespPdf.getRawCount() + it.getPdf().getRawCount());
				}
			}

			if (unknownLink.size() == 0) {
				// The node is ready to calculate its transfer function (no unknown link)

				//////////////////////////////////////////////////////////
				// Handle model calculation here, calculate transfer function
				if (node.serverResponse != null) {
					if (node.model == null) node.model = new NodeModel();
					node.model.cutoff = setCutoff(node.serverResponse);
					node.model.rawCount = node.serverResponse.getRawCount();
					node.analysisResponse = new ParametricDensity();
					node.analysisResponse.setRawCount(node.serverResponse.getRawCount());
					//node.model = [pdf:fitResult.pdf, param:fitResult.param, rawCount:node.serverResponse.rawCount]

					// calculate transfer function
					DiscreteProbDensity convPdf;

					if (distRespPdf == null) {
						convPdf = compositeRespPdf;
					} else if (compositeRespPdf == null) convPdf = distRespPdf;
					else {
						//convPdf = distRespPdf.tconv(compositeRespPdf).normalize();
						convPdf = matlab.filter(distRespPdf, compositeRespPdf).normalize();
						if (distRespPdf.getRawCount() > compositeRespPdf.getRawCount()) {
							convPdf.setRawCount(distRespPdf.getRawCount()); 
						} else {
							convPdf.setRawCount(compositeRespPdf.getRawCount());
						}
					}

					// The non-parametric transfer is the deconvolution of the output pdf and input pdf
					//log.debug("Deconvolution {} with {}",node.serverResponse.average(),convPdf.average());
					DiscreteProbDensity modelTransfer = matlab.deconv(node.serverResponse,convPdf);
					// scale down this transfer by the number of VM stacking on a processing unit
					
					// TODO: Figure out how to determine the deconvolution of stacked VMs
					if (USE_EXPANSION) {
						DiscreteProbDensity shrinkTransfer = matlab.shrink(node.serverResponse, EXPANSION_FACTOR, 1);
						DiscreteProbDensity sourceTransfer = matlab.multiDistribute(Arrays.asList(shrinkTransfer, modelTransfer), Arrays.asList(1-getExpectedCoarrival(node), getExpectedCoarrival(node)));
						modelTransfer = sourceTransfer;	
					}
					
					findAnalysisResponse(distRespPdf, compositeRespPdf, modelTransfer, node, distResp, distProb, convPdf);
				}

				// assign traffic counter if explicitly available
				if (node.serverResponse != null) {
					node.analysisResponse.getPdf().setRawCount(node.serverResponse.getRawCount());
				}

			} else {
				// finally we figure out the missing probability for the distribution link
				// need to calculate unknown
				if (compositeRespPdf == null) {
					// distribution links only
					// TODO: for now, subtract the existing dist from the actual response
					Node targetNode = unknownLink.get(0).target;
					double sumDistProb = 0;
					for (double d:distProb) { sumDistProb += d; };
					assert sumDistProb <= 1;

					// TODO: we over shoot here to remove extra noise, should do something else like lowpass filter
					log.info("calculate distribution probability for {} with root {} - dist {}", new Object[] {targetNode, node, distRespPdf});
					ParametricDensity outputResponse = new ParametricDensity(node.serverResponse, node.serverResponse.getRawCount());
					if (node.model == null) {
						node.model = new NodeModel(outputResponse);
					} else {
						node.model.outputResponse = outputResponse;
					}
					DiscreteProbDensity unknownPdf = node.model.outputResponse.getPdf().remainDistribute(10,distRespPdf).ensurePositive();
					
					targetNode.analysisResponse = new ParametricDensity(unknownPdf, null, (1.0-sumDistProb) * totalRawCount);
					targetNode.model = new NodeModel(unknownPdf, null, (1.0-sumDistProb) * totalRawCount);
					targetNode.modelpdf = unknownPdf;

					unknownLink.get(0).type.distProb = 1.0-sumDistProb;
					// recalculate current node analysis result
					log.debug("unknown link found - recalculate for {}",node);

					node.analysisResponse = null;
					node.mark = false;
					analyzeResponse(node);
				} else {
					log.error("analysis not available");
					assert (1==0);
					// need to handle other cases
				}
			}

			log.debug("{} analyzed - {}",node,node.analysisResponse);
			node.mark = true;
		}
	}

	void predictResponse(Node node) {
		Subgraph children = getChildren(node);
		Set<Link> links = children.links;
		Set<Node> childs = children.nodes;

		if (node.mark) {
			// We've already considered this node
			return;
		}

		if (childs.size() == 0 || node.edited) {
			// terminal case, should fit a distribution
			if (node.edited) {
				// Refit the model to the specified response

				// Match pdf raw counter
				log.debug("{} - {} - {}",new Object[] {node,node.serverResponse,node.serverResponse.average()});
				node.analysisResponse = new ParametricDensity(node.serverResponse);
				node.analysisResponse.getPdf().setRawCount(node.serverResponse.getRawCount());
			} else if (node.transferEdited) {
				node.analysisResponse = new ParametricDensity(node.model.pdf.shiftByValue(node.shiftValue*node.model.pdf.average()));
			} else {
				node.analysisResponse = new ParametricDensity(new DiscreteProbDensity(node.model.pdf), node.model.param.clone(), node.model.rawCount);
			}
			log.info("{} analyzed - {}",node,node.analysisResponse);
			node.mark = true;

		} else {
			// first we try to analyze all the children
			for (Node it:childs) {
				predictResponse(it);
			}

			List<ParametricDensity> compResp = new ArrayList<ParametricDensity>();
			List<ParametricDensity> distResp = new LinkedList<ParametricDensity>();
			List<Double> distProb = new LinkedList<Double>();
			List<Integer> requestCounter = new ArrayList<Integer>();
			List<Link> distLinks = new LinkedList<Link>();

			// then we attempt to combine the result based on link type
			for (Link link:links) {
				if (link.target.analysisResponse != null) {
					if (link.type instanceof CompositionDependency) {
						for (int i=0;i<link.target.requestCount;i++) {
							compResp.add(link.target.analysisResponse);
							requestCounter.add(link.target.requestCount);
						}
					} else if (link.type instanceof DistributionDependency) {
						distLinks.add(link);
						distResp.add(link.target.analysisResponse);
					}
				}
			}

			// rebalance distribution link probability
			for (Link it:distLinks) {
				distProb.add(it.type.distProb);
			}

			// first we convolve all the composite response
			DiscreteProbDensity compositeRespPdf = null;
			if (compResp.size()>0) {
				compositeRespPdf = compResp.get(0).pdf;
				for (int i=1;i<compResp.size();i++) {
					compositeRespPdf = compositeRespPdf.tconv(compResp.get(i).pdf);
					if (compositeRespPdf.getRawCount() == null) compositeRespPdf.setRawCount(0L);
					if (compResp.get(i).pdf.getRawCount() == null) compResp.get(i).pdf.setRawCount(0L);
					compositeRespPdf.setRawCount( compositeRespPdf.getRawCount()
							+ compResp.get(i).pdf.getRawCount() / requestCounter.get(i));
				}
				//log.debug("Composite size {}, avg {}",compResp.size(),compositeRespPdf.average());
			}

			// next we calculate the response from distribution link
			DiscreteProbDensity distRespPdf = null;
			if (distResp.size() > 0) {
				List<DiscreteProbDensity> dPdf = new LinkedList<DiscreteProbDensity>();
				for (ParametricDensity it:distResp) {
					dPdf.add(it.pdf);
				}
				distRespPdf = matlab.multiDistribute(dPdf,distProb);
			}

			findNodeResponse(distRespPdf, compositeRespPdf, distResp, distProb, node);

			log.debug("{} predicted - {}",node,node.analysisResponse);
			node.mark = true;
		}
	}
	
	void findAnalysisResponse(DiscreteProbDensity distRespPdf, DiscreteProbDensity compositeRespPdf, DiscreteProbDensity modelTransfer, Node node,
			List<ParametricDensity> distResp, List<Double> distProb, DiscreteProbDensity convPdf) {
		if (distRespPdf == null) {
			// Composite only
			//DiscreteProbDensity reconv = matlab.filter(compositeRespPdf,modelTransfer).ensurePositive().cutoff(node.model.cutoff); //.smooth()
			////ParametricDensity inputGev = new ParametricDensity(compositeRespPdf);

			// obtain parametric function for output
			//ParametricDensity newFit = (shapeshift)?getParamFit(reconv,nonparamPredict):getParamFitShape(reconv,inputGev.getParam()[0],nonparamPredict);
			////node.modelinput = inputGev;
			//log.debug("---{}--- Composite Parameters   = {}", node, newFit);

			//if (inputGev.getParam()[1] <= 0) inputGev.getParam()[1] = newFit.getParam()[1];
			////if (inputGev.getParam()[1] <= 0) inputGev.getParam()[1] = 1;

			// Recalculate proper transfer
			TransferFunction transfer = new TransferFunction(null, modelTransfer);
			transfer.setPdf(modelTransfer);
			log.info("---{}--- Composite Transfer = {}", node, transfer.getPdf().mode());

			// Recalculate result using transfer
			node.model.transfer = transfer;

			DiscreteProbDensity predictPdf;
			predictPdf = matlab.filter(compositeRespPdf, node.model.getTransfer().getNonparamPdf()).ensurePositive().cutoff(node.model.cutoff);
			transfer.setInputPdf(compositeRespPdf.ensurePositive().cutoff(node.model.cutoff));
			transfer.setOutputPdf(predictPdf);

			node.analysisResponse = new ParametricDensity(predictPdf);
			node.modelpdf = predictPdf;
			//node.modeloutput = newFit.param;

		} else if (compositeRespPdf == null) {
			// distribution node
			double[] averageTransfer = new double[] {0,0,0};
			//double[] maxTransfer = new double[] {0,0,0};
			List<double[]> linkTransfer = new ArrayList<double[]>();
			int distCount = 0;

			List<DiscreteProbDensity> distPdf = new LinkedList<DiscreteProbDensity>();

			for (ParametricDensity it:distResp) {
				//DiscreteProbDensity reconv = matlab.filter(it.getPdf(), modelTransfer).ensurePositive();//.smooth()

				TransferFunction transfer = new TransferFunction(null, modelTransfer);
				log.debug("---{}--- Distributed Component Transfer = {}",node,transfer);
				distCount++;

				linkTransfer.add(transfer.param);

				distPdf.add(matlab.filter(it.getPdf(), modelTransfer));
			}

			averageTransfer[0] = averageTransfer[0] / distCount;
			averageTransfer[1] = averageTransfer[1] / distCount;
			averageTransfer[2] = averageTransfer[2] / distCount;


			TransferFunction transfer = new TransferFunction(averageTransfer, linkTransfer, modelTransfer);

			log.info("{} Distributed Transfer Parameters = {}", node, transfer);

			node.model.transfer = transfer;

			DiscreteProbDensity newDistPdf = matlab.multiDistribute(distPdf,distProb).ensurePositive().cutoff(node.model.cutoff);
			node.analysisResponse = new ParametricDensity(newDistPdf);
			node.modelpdf = newDistPdf;

			transfer.setInputPdf(convPdf.ensurePositive().cutoff(node.model.cutoff));
			transfer.setOutputPdf(newDistPdf);
		} else {
			// not gonna handle this case yet
			log.error("Heterogeneous node detected (both composition/distribution links exist");
			assert (1==0);
		}		
	}
	
	void findNodeResponse(DiscreteProbDensity distRespPdf, DiscreteProbDensity compositeRespPdf, List<ParametricDensity> distResp, List<Double> distProb, Node node) {
		////////////////////////////////////////////////////////////
		// Handle Prediction here (if model is available)
		if (distRespPdf == null) {
			// composite only
			log.debug("Calculate prediction for composition node - {}",node);

			if (node.model != null) {
				//ParametricDensity inputGev = getParamFit(compositeRespPdf,nonparamPredict);
				double[] newParam = null;

				DiscreteProbDensity predictPdf = null;// = getPdf(newParam[0],newParam[1],newParam[2]);
				DiscreteProbDensity baseTransfer = (node.transferEdited)?node.model.transfer.editedNonparamPdf : node.model.transfer.nonparamPdf;
				if (USE_EXPANSION) {
					DiscreteProbDensity expandedTransfer = matlab.expand(baseTransfer, EXPANSION_FACTOR, 1);
					baseTransfer = matlab.multiDistribute(Arrays.asList(baseTransfer, expandedTransfer), Arrays.asList(1-getExpectedCoarrival(node), getExpectedCoarrival(node)));
				}
				predictPdf = matlab.filter(compositeRespPdf, baseTransfer).ensurePositive().cutoff(node.model.cutoff);
				
				log.debug("Predicted composite output avg {}",predictPdf.average());
				node.analysisResponse = new ParametricDensity(predictPdf, newParam, null);
			} else {
				log.debug("Cannot find composite model for {}, forwarding result",node);
				node.analysisResponse = new ParametricDensity(compositeRespPdf);
			}
		} else if (compositeRespPdf == null) {
			// distribution only
			log.debug("Calculate prediction for distribution node - {} - model {}",node,node.model);
			// Prediction mode, apply previously shifted parameter
			if (node.model!=null) {
				DiscreteProbDensity newDistPdf = null;
				List<DiscreteProbDensity> dPdf = new LinkedList<DiscreteProbDensity>();

				for (ParametricDensity it:distResp) {
					// find input response
					DiscreteProbDensity baseTransfer = (node.transferEdited)?node.model.transfer.editedNonparamPdf : node.model.transfer.nonparamPdf;
					if (USE_EXPANSION) {
						DiscreteProbDensity expandedTransfer = matlab.expand(baseTransfer, EXPANSION_FACTOR, 1);
						baseTransfer = matlab.multiDistribute(Arrays.asList(baseTransfer, expandedTransfer), Arrays.asList(1-getExpectedCoarrival(node), getExpectedCoarrival(node)));
					}
					dPdf.add(matlab.filter(it.pdf, baseTransfer));
				}
				newDistPdf = matlab.multiDistribute(dPdf,distProb).ensurePositive().cutoff(node.model.cutoff);
				node.analysisResponse = new ParametricDensity(newDistPdf);
			} else {
				log.debug("Cannot find distributed model for {}, forwarding result",node);
				node.analysisResponse = new ParametricDensity(distRespPdf);
			}
		} else {
			/////////////////
			// Not handle this case yet
			log.error("Heterogeneous link state detected, abort");
			assert (1==0);
		}
	}
}