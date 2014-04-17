package main.java.indicator;
import java.util.*;

import main.java.data.MarketData;

/**
 * Calculate EMA
 * EMA algorithm: http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages
 * 
 * @author Zhongqiang Shen
 *
 */
public class Ema extends Indicator {
	private int n;
	private ArrayList<Double> buffer;
	private ArrayList<MarketData> timeSeries;
	
	/**
	 * Constructor
	 * 
	 * @param n - number of days (e.g. n = 10 means 10 day EMA)
	 */
	public Ema(int n) {
		this.n = n;
		this.buffer = new ArrayList<Double>();
		this.timeSeries = new ArrayList<MarketData>();
		
	}
	
	public void Update(MarketData data) {
		this.timeSeries.add(data);
		int len = this.timeSeries.size();
		if(len < n) {
			buffer.add(0.0);
			return;
		}
		
		if(len == n) {
			double sum = 0;
			for(int i = 0; i <= n-1; i++) {
				sum += timeSeries.get(i).getClose();
			}
			buffer.add(sum / n);
			return;
		}
		
		if(len > n) {
			double k = (float) 2 / (n+1);
			double prevEma = buffer.get(len - 2);
			double ema = (timeSeries.get(len - 1).getClose() - prevEma) * k + prevEma;
			buffer.add(ema);
			return;
		}
	}
	
	public double getEma(int i) throws Exception {
		int len = timeSeries.size();
		
		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return buffer.get(i);
	}
}
