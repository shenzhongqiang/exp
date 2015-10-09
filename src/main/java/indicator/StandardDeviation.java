package main.java.indicator;
import java.util.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import main.java.data.MarketData;;

/**
 * Calculate average true range
 *
 * @author Zhongqiang Shen
 *
 */
public class StandardDeviation extends Indicator {
	private int n;
	private ArrayList<MarketData> timeSeries;
	// since ATR uses recursion calculation, store previous values in buffer to prevent duplicate calculation
	private ArrayList<Double> buffer;

	/**
	 * Constructor
	 *
	 * @param n - number of days (e.g. n=20 means 20 day ATR)
	 */
	public StandardDeviation(int n) {
		this.n = n;
		this.timeSeries = new ArrayList<MarketData>();
		this.buffer = new ArrayList<Double>();
	}

	@Override
	public void Update(MarketData data) {
		this.timeSeries.add(data);

		int len = this.timeSeries.size();
        SummaryStatistics ss = new SummaryStatistics();

		try {
			if(len < n) {
				buffer.add(0.0);
				return;
			}

			if(len >= n) {
                for(int i=len-n; i<len; i++) {
                    ss.addValue(this.timeSeries.get(i).getClose());
                }
                double stdev = ss.getStandardDeviation();
				buffer.add(stdev);
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
	public double getStandardDeviation(int i) throws Exception {
		int len = timeSeries.size();

		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return buffer.get(i);
	}
}
