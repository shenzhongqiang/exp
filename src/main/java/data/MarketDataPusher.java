package main.java.data;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import au.com.bytecode.opencsv.CSVReader;
import main.java.strategy.Strategy;
import main.java.subscriber.*;
import main.java.exceptions.*;

/**
 * Push market data to upper layer like strategy or order
 *
 * @author Zhongqiang Shen
 */
public class MarketDataPusher {
	private String product;
	private int curr;
	private int timeframe;
	private Date start;
	private Date end;
	private ArrayList<Subscriber> strategies;
	private ArrayList<Subscriber> orders;
	private ArrayList<MarketData> bidBuffer;
	private ArrayList<MarketData> askBuffer;

	/**
	 * Constructor
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param timeframe - timeframe (e.g. 15m)
	 * @param start - start time
	 * @param end - end time
	 */
	public MarketDataPusher(String product, int timeframe,
			String start, String end, File historyFile) {
		this.product = product;
		this.timeframe = timeframe;

		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		try {
			this.start = ft.parse(start);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse start time " + start);
		}

		try {
			this.end = ft.parse(end);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse end time " + end);
		}

		if(this.start.compareTo(this.end) > 0) {
			return;
		}

		this.askBuffer = new ArrayList<MarketData>();
		this.bidBuffer = new ArrayList<MarketData>();
		// store all market data ranging from start to end into bid and ask buffer
        String sTimeframe = String.format("m%d", timeframe);
        HashMap<String, ArrayList<MarketData>> hm = readMarketData(product, historyFile);
        for(int i=0; i < hm.get("ask").size(); i++) {
            Date date = hm.get("ask").get(i).getStartDate();
            if(date.compareTo(this.start) >= 0 && date.compareTo(this.end) < 0) {
                this.askBuffer.add(hm.get("ask").get(i));
                this.bidBuffer.add(hm.get("bid").get(i));
            }
        }

		// initialize array list of subsribers
		this.strategies = new ArrayList<Subscriber>();
		this.orders = new ArrayList<Subscriber>();
		curr = 0;
	}

	/**
	 * Notify subscribers
	 *
	 * @return true if there is still upcoming data in buffer, false if it reaches the end of buffer
	 */
	public boolean Notify() {
		if(curr < this.bidBuffer.size()) {
			MarketData bid = this.bidBuffer.get(curr);
			MarketData ask = this.askBuffer.get(curr);

			// notify all orders
			for(Subscriber s: this.orders) {
				s.Update(product, bid, ask);
			}

			// notify all strategies
			for(Subscriber s: this.strategies) {
				s.Update(product, bid, ask);
			}

			curr++;
			return true;
		}

		return false;
	}

	public void AttachStrategy(Subscriber o) {
		this.strategies.add(o);
	}

	public void AttachOrder(Subscriber o) {
		this.orders.add(o);
	}

	public void DetachStrategy(Subscriber o) {
		this.strategies.remove(o);
	}

	public void DetachOrder(Subscriber o) {
		this.orders.remove(o);
	}

	public static HashMap<String, ArrayList<MarketData>> readMarketData(String product, File file) {

		ArrayList<MarketData> bids = new ArrayList<MarketData>();
		ArrayList<MarketData> asks = new ArrayList<MarketData>();
		try {
			CSVReader reader = new CSVReader(new FileReader(file));
			String[] parts;

			while((parts = reader.readNext()) != null) {
				String start = parts[0];

				double bidOpen = Double.parseDouble(parts[1]);
				double bidClose = Double.parseDouble(parts[2]);
				double bidHigh = Double.parseDouble(parts[3]);
				double bidLow = Double.parseDouble(parts[4]);

				double askOpen = Double.parseDouble(parts[5]);
				double askClose = Double.parseDouble(parts[6]);
				double askHigh = Double.parseDouble(parts[7]);
				double askLow = Double.parseDouble(parts[8]);

				int volume = Integer.parseInt(parts[9]);

				MarketData bid = new MarketData(product, start,
					bidOpen, bidClose, bidHigh, bidLow, volume);
				MarketData ask = new MarketData(product, start,
						askOpen, askClose, askHigh, askLow, volume);
                /* comment out bid ask price validation, because sometimes bid > ask
                if(bidOpen > askOpen || bidClose > askClose ||
                    bidHigh > askHigh || bidLow > askLow) {
                    System.out.println(bid);
                    System.out.println(ask);
                    throw new IllegalBidAsk();
                }
                */

				bids.add(bid);
				asks.add(ask);
			}

			reader.close();

		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file " + file.getPath());
		}
		catch(IOException ex) {
			System.out.println("Error reading file " + file.getPath());
		}

		HashMap<String, ArrayList<MarketData>> hm = new HashMap<String, ArrayList<MarketData>>();
        Collections.reverse(asks);
        Collections.reverse(bids);
		hm.put("ask", asks);
		hm.put("bid", bids);
		return hm;
	}

	private static TreeMap<Date, String> getDateFileMap() {
		String path = "src/main/java/history/EURUSD5M";
		File folder = new File(path);
		File[] files = folder.listFiles();
		String pattern = "(\\d{4}-\\d{2}-\\d{2})";
		Pattern r = Pattern.compile(pattern);
		TreeMap<Date, String> tm = new TreeMap<Date, String>();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");

		for(int i = 0; i < files.length; i++) {
			if(files[i].isFile()) {
				String filePath = files[i].getPath();
				Matcher m = r.matcher(filePath);

				if(m.find()) {
					try {
						Date tradingDate = ft.parse(m.group(1));
						tm.put(tradingDate, filePath);
					}
					catch(ParseException e) {
						System.out.println("Unable to parse date using " + ft);
					}

				}
			}
		}
		return tm;
	}

	public int getBarNum() {
		return this.bidBuffer.size();
	}
}
