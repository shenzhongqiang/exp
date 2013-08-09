package indicator;

import java.util.ArrayList;
import data.MarketData;

/**
 * Get high or low price of the specified number of days
 * 
 * @author Zhongqiang Shen
 */
public class HighLow extends Indicator {
	private int n;
	
	/**
	 * Constructor
	 * 
	 * @param n - the specified number of days
	 */
	public HighLow(int n) {
		this.n = n;
	}
	
	/**
	 * Get high as if the specified index has the latest market data
	 * @param md - array list of market data {@link MarketData}
	 * @param i - the specified index
	 * @return high as if specified index has the latest market data
	 * @throws Exception
	 */
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
	
	/**
	 * Get low as if the specified index has the latest market data
	 * @param md - array list of market data {@link MarketData}
	 * @param i - the specified index
	 * @return low as if specified index has the latest market data
	 * @throws Exception
	 */
	public double getLow(ArrayList<MarketData> md, int i) throws Exception {
		int len = md.size();
		if(len < n || i < n - 1) {
			return 0;
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range, max allowed is " + len);
		}
		
		double low = md.get(i).getLow();
		for(int k=i; k > i - n; k--) {
			low = Math.min(md.get(k).getLow(), low);
		}
		return low;
	}
}
