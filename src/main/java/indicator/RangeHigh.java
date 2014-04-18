package main.java.indicator;

import java.util.ArrayList;

import main.java.data.MarketData;

/**
 * Get high price of the specified number of days
 *
 * @author Zhongqiang Shen
 */
public class RangeHigh extends Indicator {
	private int n;
	private ArrayList<MarketData> timeSeries;
	private ArrayList<Double> buffer;

	/**
	 * Constructor
	 *
	 * @param n - the specified number of days
	 */
	public RangeHigh(int n) {
		this.n = n;
		this.timeSeries = new ArrayList<MarketData>();
		this.buffer = new ArrayList<Double>();
	}

	@Override
	public void Update(MarketData data) {
		this.timeSeries.add(data);

		int len = this.timeSeries.size();
		if(len < n) {
			buffer.add(0.0);
			return;
		}

		double high = 0;
		for(int k = len - 1; k >= len - n; k--) {
			high = Math.max(timeSeries.get(k).getHigh(), high);
		}
		buffer.add(high);
	}

	/**
	 * Get high as if the specified index has the latest market data
	 *
	 * @param i - the specified index
	 * @return high as if specified index has the latest market data
	 * @throws Exception
	 */
	public double getRangeHigh(int i) throws Exception {
		int len = timeSeries.size();

		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return buffer.get(i);
	}
}