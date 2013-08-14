package indicator;
import java.util.*;

import data.MarketData;;

/**
 * Calculate average true range
 * 
 * @author Zhongqiang Shen
 * 
 */
public class AverageTrueRange extends Indicator {
	private int n;
	private ArrayList<MarketData> timeSeries;
	// since ATR uses recursion calculation, store previous values in buffer to prevent duplicate calculation
	private ArrayList<Double> buffer;
	
	/**
	 * Constructor
	 * 
	 * @param n - number of days (e.g. n=20 means 20 day ATR)
	 */
	public AverageTrueRange(int n) {
		this.n = n;
		this.timeSeries = new ArrayList<MarketData>();
		this.buffer = new ArrayList<Double>();
	}
	
	@Override
	public void Update(MarketData data) {
		this.timeSeries.add(data);
		
		int len = this.timeSeries.size();
		
		try {
			if(len < n) {
				buffer.add(0.0);
				return;
			}
			
			if(len == n) {
				double sum = 0;
				for(int i = 0; i <= n-1; i++) {
					sum += this.getTr(i);
				}
				buffer.add(sum / n);
				return;
			}
			
			if(len > n) {
				double prevAtr = buffer.get(len - 2);
				double tr = this.getTr(len - 1);
				double atr = (prevAtr * (n - 1) + tr) /n;
				buffer.add(atr);
				return;
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	/**
	 * Get ATR as if specified index has the latest market data
	 * 
	 * @param i - the specified index, zero based
	 * @return ATR as if specified index has the latest market data
	 * @throws Exception
	 */
	public double getAtr(int i) throws Exception {
		int len = timeSeries.size();
		
		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return buffer.get(i);
	}
	
	/**
	 * Get true range of the specified index
	 *
	 * @param i - the specified index, zero based
	 * @return true range of the specified index
	 * @throws Exception
	 */
	public double getTr(int i) throws Exception {
		int len = timeSeries.size();
		if(len == 0) {
			throw new Exception("array list is empty");
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		
		double tr = 0;
		if(i == 0) {
			MarketData curr = timeSeries.get(i);
			tr = curr.getHigh() - curr.getLow();
		}
		else {
			MarketData curr = timeSeries.get(i);
			MarketData prev = timeSeries.get(i - 1);
			double a1 = curr.getHigh() - curr.getLow();
			double a2 = curr.getHigh() - prev.getClose();
			double a3 = prev.getClose() - curr.getLow();
			tr = Math.max(a1, a2);
			tr = Math.max(tr, a3);
		}
		return tr;
	}
}
