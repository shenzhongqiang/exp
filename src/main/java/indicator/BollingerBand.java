package main.java.indicator;
import java.util.*;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import main.java.exceptions.*;
import main.java.data.MarketData;

/**
 * Calculate BB
 * BB algorithm: http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:bollinger_bands
 *
 * @author Zhongqiang Shen
 *
 */
public class BollingerBand extends Indicator {
	private int n;
	private ArrayList<Double> upper;
	private ArrayList<Double> lower;
	private ArrayList<Double> middle;
	private ArrayList<MarketData> timeSeries;

	/**
	 * Constructor
	 *
	 * @param n - number of days (e.g. n = 10 means 10 day EMA)
	 */
	public BollingerBand(int n) {
		this.n = n;
		this.upper = new ArrayList<Double>();
		this.lower = new ArrayList<Double>();
		this.middle = new ArrayList<Double>();
		this.timeSeries = new ArrayList<MarketData>();

	}

	public void Update(MarketData data) {
		this.timeSeries.add(data);
		int len = this.timeSeries.size();
        SummaryStatistics ss = new SummaryStatistics();

		if(len < n) {
			upper.add(0.0);
			lower.add(0.0);
			middle.add(0.0);
			return;
		}

		if(len >= n) {
            for(int i=len-n; i<len; i++) {
                ss.addValue(this.timeSeries.get(i).getClose());
            }
            double mean = ss.getMean();
            double stdev = ss.getStandardDeviation();
            middle.add(mean);
            upper.add(mean + stdev*2);
            lower.add(mean - stdev*2);
            return;
		}
	}

	public double getUpperBand(int i) throws Exception {
		int len = timeSeries.size();

		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return upper.get(i);
	}

	public double getLowerBand(int i) throws Exception {
		int len = timeSeries.size();

		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return lower.get(i);
	}

	public double getMiddleBand(int i) throws Exception {
		int len = timeSeries.size();

		if(i >= len) {
			throw new Exception(i + " is out of range");
		}
		return middle.get(i);
	}

}

