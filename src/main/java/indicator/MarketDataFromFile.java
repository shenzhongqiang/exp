package main.java.indicator;
import java.util.*;
import java.io.*;
import main.java.data.*;
import main.java.exceptions.*;

/**
 * Get time series in file
 *
 * @author Zhongqiang Shen
 *
 */
public class MarketDataFromFile extends Indicator {
    private int curr;
    private String product;
    private int timeframe;
    private ArrayList<MarketData> dataBuffer;

	/**
	 * Constructor
	 *
	 * @param product - the specified product
     * @param timeframe - in minutes
	 * @param historyFile - the history file
	 */
	public MarketDataFromFile(String product, int timeframe, File historyFile) {
        this.curr = -1;
		this.product = product;
		this.timeframe = timeframe;
		HashMap<String, ArrayList<MarketData>> hm = MarketDataPusher.readMarketData(product, historyFile);
        this.dataBuffer = new ArrayList<MarketData>(hm.get("ask"));
	}

	@Override
	public void Update(MarketData data) {
        if(this.dataBuffer.isEmpty()) {
            throw new MissingMarketData();
        }

        while(this.curr+1 < this.dataBuffer.size()) {
            Date currDate = this.dataBuffer.get(this.curr+1).getStartDate();
            Calendar barEnd = Calendar.getInstance();
            barEnd.setTime(currDate);
            barEnd.add(Calendar.MINUTE, timeframe);
            if(barEnd.getTime().compareTo(data.getStartDate()) > 0) {
                break;
            }
            this.curr++;
        }
	}

	/**
	 * Get market data
	 *
	 * @return market data {@link ArrayList<MarketData>}
	 */
	public ArrayList<MarketData> getMarketData() {
        return new ArrayList<MarketData>(this.dataBuffer.subList(0, this.curr+1));
	}
}


