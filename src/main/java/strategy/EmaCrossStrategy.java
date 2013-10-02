package strategy;

import java.util.*;
import data.MarketData;
import indicator.*;
import model.*;
import subscriber.*;
import order.Order;

/**
 * EMA Cross Strategy
 * 
 * @author Zhongqiang Shen
 */
public class EmaCrossStrategy extends Strategy implements Subscriber {
	// state 
	// 0 - no open position
	// 1 - one unit
	// 2 - two unit2
	// 3 - three units
	// 4 - four units
	private int state = 0;
	private double stopPrice = 0;
	private double r = 0;
	private Ema ema8;
	private Ema ema21;
	private Ema ema55;
	private RangeHigh high8;
	private RangeLow low8;
	
	/**
	 * Constructor
	 * 
	 * @param order - the specified order {@link Order}
	 */
	public EmaCrossStrategy(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
		this.ema8 = new Ema(8);
		this.ema21 = new Ema(21);
		this.ema55 = new Ema(55);
		this.high8 = new RangeHigh(8);
		this.low8 = new RangeLow(8);
	}
	
	/**
	 * Update market data and run strategy
	 * 
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
	@Override
	public void Update(String product, MarketData bid, MarketData ask) {
		bidTs.add(bid);
		askTs.add(ask);
		ema8.Update(bid);
		ema21.Update(bid);
		ema55.Update(bid);
		high8.Update(bid);
		low8.Update(bid);
		Run(product);
	}
	
	/**
	 * Run strategy
	 * 
	 * @param product - the specified product (e.g. EURUSD)
	 */
	public void Run(String product) {
		int i = bidTs.size() - 1;

		
		/*
		System.out.println(String.format("close:%f, bid low:%f, high:%f, open:%f, ask high:%f", 
				bidTs.get(i).getClose(), 
				bidTs.get(i).getLow(), 
				bidTs.get(i).getHigh(), 
				bidTs.get(i).getOpen(),
				askTs.get(i).getHigh()));
		*/
		// skip first 100 market data to give room for calculating indicators
		if(i < 100) {
			return;
		}
		
		try {
			//check if has position
			if(! order.HasPosition(product)) {
				state = 0;
			}
			
			double prevEma8 = ema8.getEma(i - 1);
			double prevEma21 = ema21.getEma(i - 1);
			double currEma8 = ema8.getEma(i);
			double currEma21 = ema21.getEma(i);
			
			boolean isCrossed = prevEma8 < prevEma21 && currEma8 > currEma21; 
			if(state == 0 && isCrossed) {
				// buy one unit
				double ask = askTs.get(i).getClose();
				double rangeLow = low8.getRangeLow(i);
				stopPrice = rangeLow - (ask - rangeLow) * 0.2;
				r = ask - stopPrice;
				String entryTime = askTs.get(i).getStart();
				int unit = (int) (0.02 * order.getAccount().getBalance() / (ask - stopPrice) / 1000);
				order.MarketBuy(product, entryTime, ask, unit);
				order.StopSell(product, entryTime, stopPrice, unit);
				state = 1;
				
				System.out.println(String.format("r:%f, rangeLow:%f. market buy %d at %f. stop at %f", r, rangeLow, unit, ask, stopPrice));
			}
			else if(state == 1) {
				double high = askTs.get(i).getHigh();
				if(high > stopPrice + 2* r) {
					List<PendingOrder> list = order.getStopSellOrders(product);
					PendingOrder po = list.get(0);
					order.UpdatePendingOrder(po, po.getAmount(), stopPrice + r);
					stopPrice = stopPrice + r;
				}
				
				//System.out.println(String.format("adding unit. market buy %d at %f", unit, entryPrice));
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}
	

}
