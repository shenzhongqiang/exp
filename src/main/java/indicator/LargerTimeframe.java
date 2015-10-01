package main.java.indicator;
import java.util.*;
import main.java.data.MarketData;
import main.java.exceptions.*;

/**
 * Calculate time series in larger timeframe
 *
 * @author Zhongqiang Shen
 *
 */
public class LargerTimeframe extends Indicator {
    private int curr;
    private int timeframe; // in minutes
	private ArrayList<MarketData> buffer;
	private ArrayList<MarketData> mergedTimeSeries;

	/**
	 * Constructor
	 *
	 * @param n - number of bars to merge (e.g. n=3 means merge 3 bars into one)
	 */
	public LargerTimeframe(int timeframe) {
        this.curr = -1;
		this.timeframe = timeframe;
		this.buffer = new ArrayList<MarketData>();
		this.mergedTimeSeries = new ArrayList<MarketData>();
	}

	@Override
	public void Update(MarketData data) {
        if(this.buffer.isEmpty()) {
            this.buffer.add(data);
        }
        else {
            Calendar start = Calendar.getInstance();
            start.setTime(this.buffer.get(0).getStartDate());
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MINUTE, timeframe);
            Calendar dataTime = Calendar.getInstance();
            dataTime.setTime(data.getStartDate());
            if(dataTime.compareTo(end) >= 0){
                double open = this.getMergedOpen();
                double close = this.getMergedClose();
                double high = this.getMergedHigh();
                double low = this.getMergedLow();
                int volume = this.getMergedVolume();
                MarketData mergedData = new MarketData(data.getProduct(),
                    data.getStart(), open, close, high, low, volume);
                this.mergedTimeSeries.add(mergedData);
                this.curr++;
                this.buffer.clear();
            }
            this.buffer.add(data);
        }
	}

	/**
	 * Get merged market data
	 *
	 * @return market data {@link MarketData}
	 */
	public MarketData getMarketData() {
        if(this.curr == -1) {
            throw new NoMarketData();
        }

        return this.mergedTimeSeries.get(this.curr);
	}

    private double getMergedOpen() {
        int size = this.buffer.size();
        if(size == 0) {
            throw new NoDataInBuffer();
        }
        return this.buffer.get(0).getOpen();
    }

    private double getMergedClose() {
        int size = this.buffer.size();
        if(size == 0) {
            throw new NoDataInBuffer();
        }
        return this.buffer.get(size-1).getClose();
    }

    private double getMergedHigh() {
        int size = this.buffer.size();
        if(size == 0) {
            throw new NoDataInBuffer();
        }
        double high = this.buffer.get(0).getHigh();
        for(int i=0; i<size; i++) {
            high = Math.max(this.buffer.get(i).getHigh(), high);   
        }
        return high;
    }

    private double getMergedLow() {
        int size = this.buffer.size();
        if(size == 0) {
            throw new NoDataInBuffer();
        }
        double low = this.buffer.get(0).getLow();
        for(int i=0; i<size; i++) {
            low = Math.min(this.buffer.get(i).getLow(), low);   
        }
        return low;
    }

    private int getMergedVolume() {
        int size = this.buffer.size();
        if(size == 0) {
            throw new NoDataInBuffer();
        }
        int volume = 0;
        for(int i=0; i<size; i++) {
            volume += this.buffer.get(i).getVolume();
        }
        return volume;
    }
}

