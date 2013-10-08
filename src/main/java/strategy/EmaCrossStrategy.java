package strategy;

import java.util.*;
import java.text.*;

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
	// 1 - has position
	
	private int state = 0;
	private double stopPrice = 0;
	private double takeProfit = 0;
	private double r = 0;
	private int unit = 0;
	private Ema ema8;
	private Ema ema21;
	private Ema ema55;
	private Ema ema144;
	private RangeHigh high8;
	private RangeLow low8;
	private RangeHigh high21;
	private int positionId = 0;
	
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
		this.ema144 = new Ema(144);
		this.high8 = new RangeHigh(8);
		this.low8 = new RangeLow(8);
		this.high21 = new RangeHigh(21);
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
		ema144.Update(bid);
		high8.Update(bid);
		low8.Update(bid);
		high21.Update(bid);
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
			boolean hasPosition = order.HasPosition(product);
			if(!hasPosition) {
				state = 0;
			}
			
			// get day of current bar. if it is Friday, close position before end of day.
			String start = bidTs.get(i).getStart();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date dt = ft.parse(start);
				Calendar cal = Calendar.getInstance();
				cal.setTime(dt);
				int day = cal.get(Calendar.DAY_OF_WEEK);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int min = cal.get(Calendar.MINUTE);
				if(day == 6 && hour == 16 && min >= 54 && hasPosition) {
					double bid = bidTs.get(i).getClose();
					order.MarketSell(product, start, bid, unit, this.positionId);
					order.CancelSellStopOrders(this.positionId);
					System.out.println("Sell on friday at " + bid);
					state = 0;
				}
			}
			catch(ParseException e) {
				System.out.println("Unable to parse date using " + ft);
			}
			
			
			double prevEma8 = ema8.getEma(i - 1);
			double prevEma21 = ema21.getEma(i - 1);
			double currEma8 = ema8.getEma(i);
			double currEma21 = ema21.getEma(i);
			double currEma55 = ema55.getEma(i);
			double currEma144 = ema144.getEma(i);
			double ask = askTs.get(i).getClose();
			double bid = bidTs.get(i).getClose();
			
			boolean crossedUp = prevEma8 < prevEma21 && currEma8 > currEma21;
			boolean crossedDown = prevEma8 > prevEma21 && currEma8 < currEma21;
			boolean isUpTrend = currEma21 > currEma55;
			if(state == 0 && crossedUp && isUpTrend) {
				// buy one unit
				double rangeLow = low8.getRangeLow(i);
				double rangeHigh = high21.getRangeHigh(i);
				
				stopPrice = rangeLow - (ask - rangeLow) * 0.2;
				takeProfit = rangeHigh * 2 - rangeLow;
				double rr = (takeProfit - ask) / (ask - stopPrice);
				r = ask - stopPrice;
				if(rr >= 1.5 && r > 0.0010) {
					String entryTime = askTs.get(i).getStart();
					unit = (int) (0.02 * order.getAccount().getBalance() / (ask - stopPrice) / 1000);
					this.positionId = order.MarketBuy(product, entryTime, ask, unit);
					order.StopSell(product, entryTime, stopPrice, unit, this.positionId);
					state = 1;
					System.out.println(String.format("r:%f, rr:%f, rangeLow:%f, rangeHigh:%f. market buy %d at %f. SL at %f. TP at %f", r, rr, rangeLow, rangeHigh, unit, ask, stopPrice, takeProfit));
				}
			}
			
			else if(state == 1) {
				double high = askTs.get(i).getHigh();
				String exitTime = bidTs.get(i).getStart();
				
				if(high > takeProfit) {
					order.MarketSell(product, exitTime, this.takeProfit, this.unit, this.positionId);
					order.CancelAllPendingOrders(this.positionId);
				}
				/*else if(crossedDown) {
					order.MarketSell(product, exitTime, bid, this.unit, this.positionId);
					order.CancelAllPendingOrders(this.positionId);
				}*/
				/*
				else {
					if(high > stopPrice + 2 * r) {
						List<PendingOrder> list = order.getStopSellOrders(this.positionId);
						PendingOrder po = list.get(0);
						order.UpdatePendingOrder(po, po.getAmount(), stopPrice + r);
						stopPrice = stopPrice + r;
					}
					
				}
				*/
				
				//System.out.println(String.format("adding unit. market buy %d at %f", unit, entryPrice));
			}
			
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}
	

}
