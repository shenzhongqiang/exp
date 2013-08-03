package indicator;

import java.util.ArrayList;
import data.MarketData;

public class HighLow extends Indicator {
	private int n;
	public HighLow(int n) {
		this.n = n;
	}
	
	public double getHigh(ArrayList<MarketData> md, int i) throws Exception {
		int len = md.size();
		if(len < n || i < n - 1) {
			return 0;
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range, max allowed is " + len);
		}
		
		double high = 0;
		for(int k=i; k > i - n; k--) {
			high = Math.max(md.get(k).getHigh(), high);
		}
		return high;
	}
	
	public double getLow(ArrayList<MarketData> md, int i) throws Exception {
		int len = md.size();
		if(len < n || i < n - 1) {
			return 0;
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range, max allowed is " + len);
		}
		
		double low = 0;
		for(int k=i; k > i - n; k--) {
			low = Math.max(md.get(k).getLow(), low);
		}
		return low;
	}
}
