package main.java.data;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import au.com.bytecode.opencsv.CSVReader;
import main.java.strategy.Strategy;
import main.java.subscriber.*;

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
			String start, String end) {
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

		TreeMap<Date, String> tm = getDateFileMap();
		Set<Date> dates = tm.keySet();
		this.askBuffer = new ArrayList<MarketData>();
		this.bidBuffer = new ArrayList<MarketData>();
		// store all market data ranging from start to end into bid and ask buffer
		for(Date date: dates) {
			String filePath = tm.get(date);

			if(date.compareTo(this.start) >= 0 && date.compareTo(this.end) <= 0) {
				HashMap<String, ArrayList<MarketData>> hm = readMarketData(product, filePath);
				this.askBuffer.addAll(hm.get("ask"));
				this.bidBuffer.addAll(hm.get("bid"));
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

	private static HashMap<String, ArrayList<MarketData>> readMarketData(String product, String filePath) {

		ArrayList<MarketData> bids = new ArrayList<MarketData>();
		ArrayList<MarketData> asks = new ArrayList<MarketData>();
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath));
			String[] parts;

			while((parts = reader.readNext()) != null) {
				String start = parts[0];
				String end = parts[1];

				double bidOpen = Double.parseDouble(parts[2]);
				double bidClose = Double.parseDouble(parts[3]);
				double bidHigh = Double.parseDouble(parts[4]);
				double bidLow = Double.parseDouble(parts[5]);

				double askOpen = Double.parseDouble(parts[6]);
				double askClose = Double.parseDouble(parts[7]);
				double askHigh = Double.parseDouble(parts[8]);
				double askLow = Double.parseDouble(parts[9]);

				int volume = Integer.parseInt(parts[10]);
				MarketData bid = new MarketData(product, start, end,
					bidOpen, bidClose, bidHigh, bidLow, volume);
				MarketData ask = new MarketData(product, start, end,
						askOpen, askClose, askHigh, askLow, volume);

				bids.add(bid);
				asks.add(ask);
			}

			reader.close();

		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file " + filePath);
		}
		catch(IOException ex) {
			System.out.println("Error reading file " + filePath);
		}

		HashMap<String, ArrayList<MarketData>> hm = new HashMap<String, ArrayList<MarketData>>();
		hm.put("ask", asks);
		hm.put("bid", bids);
		return hm;
	}

	private static TreeMap<Date, String> getDateFileMap() {
		String path = "marketdata\\FX_FXCM_Demo_EUR-USD_2013_EST_5";
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
