package main.java.indicator;
import java.util.*;
import java.io.*;
import main.java.data.*;
import main.java.exceptions.*;

/**
 * Compare with another product
 *
 * @author Zhongqiang Shen
 *
 */
public class CompareWith extends Indicator {
	private int curr;
	private ArrayList<MarketData> dataBuffer;

	/**
	 * Constructor
	 *
	 * @param product - the specified product
	 */
	public CompareWith(String product, File historyFile) {
		this.curr = 0;
        HashMap<String, ArrayList<MarketData>> hm  = MarketDataPusher.readMarketData(product, historyFile);
		this.dataBuffer = new ArrayList<MarketData>(hm.get("ask"));
	}

	public void Update(MarketData data) {
        while(this.curr < this.dataBuffer.size()) {
            Date currDate = this.dataBuffer.get(this.curr).getStartDate();
            if(currDate.compareTo(data.getStartDate()) >= 0) {
                break;
            }
            this.curr++;
        }
        Date currDate = this.dataBuffer.get(this.curr).getStartDate();
        if(currDate.compareTo(data.getStartDate()) > 0) {
            throw new MissingMarketData();
        }
	}

	public MarketData getMarketData() {
        return this.dataBuffer.get(this.curr);
	}
}

