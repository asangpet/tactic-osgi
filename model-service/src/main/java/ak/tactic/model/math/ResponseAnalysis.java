package ak.tactic.model.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ak.tactic.data.ResponseInfo;

public class ResponseAnalysis {
	// Find co-arrival of b with respsect to a (e.g how many request as a percentage of a overlap with b)
	public static double findCoarrivalProb(List<ResponseInfo> a, List<ResponseInfo> b) {
		double co = 0;
		Collections.sort(a, ResponseInfo.getRequestTimeComparator());
		Collections.sort(b, ResponseInfo.getDeadlineComparator());
		int begin = 0;
		for (ResponseInfo ra:a) {
			for (int idx=begin;idx<b.size();idx++) {
				ResponseInfo rb = b.get(idx);
				if (rb.getRequestTime() > ra.getRequestTime()+ra.getResponseTime()) break;
				if (rb.getRequestTime() + rb.getResponseTime() < ra.getRequestTime()) begin = idx;
				if (ra.isOverlap(rb)) {
					co++; break;
				}
			}
		}
		return co/a.size();
	}
	
	// Find co-arrival of b with respsect to a (e.g how many request as a percentage of a overlap with b)
	public static double findCoarrivalProb(Iterator<ResponseInfo> a, Iterator<ResponseInfo> b) {
		int MAX_BUFFER = 10000;
		double co = 0;
		int begin = 0;
		int tail = 0;
		int aCount = 0;
		ResponseInfo[] buffer = new ResponseInfo[MAX_BUFFER];
		while (a.hasNext()) {
			aCount++;
			ResponseInfo ra = a.next();
			
			for (int idx=begin; ;idx++) {
				if (idx == MAX_BUFFER) {
					idx = 0;
				}
				ResponseInfo rb;
				if (idx == tail) {
					if (b.hasNext()) {
						buffer[tail++] = b.next();
						if (tail == MAX_BUFFER) {
							tail = 0;
						}						
					} else break;					
				}
				rb = buffer[idx];
				if (ra.isOverlap(rb)) {
					co++; break;
				}
				
				if (rb.getRequestTime() > ra.getRequestTime()+ra.getResponseTime()) break;
				if (rb.getRequestTime() + rb.getResponseTime() < ra.getRequestTime()) {
					// switch entry with the correct one and increment the beginning index
					buffer[idx] = buffer[begin];
					begin++;
					if (begin == MAX_BUFFER) {
						begin = 0;
					}
				}
			}
		}
		return co/aCount;
	}
	
	static List<ResponseInfo> mockResponse(double[] timepair) {
		List<ResponseInfo> list = new ArrayList<ResponseInfo>(timepair.length/2);
		for (int i=0;i<timepair.length;i+=2) {
			ResponseInfo r = new ResponseInfo();
			r.setRequestTime(timepair[i]);
			r.setResponseTime(timepair[i+1]-timepair[i]);
			list.add(r);
		}
		return list;
	}
	
	public static void main(String[] args) {
		ResponseAnalysis a = new ResponseAnalysis();
		List<ResponseInfo> r1 = mockResponse(new double[] {0,1, 2,3, 4,5, 6,7});
		List<ResponseInfo> r2 = mockResponse(new double[] {0,1, 3.1,6.1});
		System.out.println(a.findCoarrivalProb(r1.iterator(), r2.iterator()));
	}
}
