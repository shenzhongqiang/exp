package strategy;

import java.util.*;

import data.MarketData;
import indicator.*;
import model.*;
import subscriber.*;
import order.Order;

/**
 * Ideal Turtle Strategy
 * 
 * @author Zhongqiang Shen
 */
public class IdealTurtleStrategy extends Strategy {
	// state 
	// 0 - no open position
	// 1 - one unit
	// 2 - two unit2
	// 3 - three units
	// 4 - four units
	private int state = 0;
	private double entryPrice = 0;
	// N = value of ATR
	private double n = 0;
	// indicator - ATR
	private AverageTrueRange atr;
	// indicator - 20 day high
	private RangeHigh high20;
	// indicator - 10 day low
	private RangeLow low10;
	// unit when opening position, will be used later when adding positions
	private int unit = 0;
	
	/**
	 * Constructor
	 * 
	 * @param order - the specified order {@link Order}
	 */
	public IdealTurtleStrategy(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
		this.atr = new AverageTrueRange(20);
		this.high20 = new RangeHigh(20);
		this.low10 = new RangeLow(10);
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
		atr.Update(bid);
		high20.Update(bid);
		low10.Update(bid);
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
		
		// skip first 50 market data to give room for calculating indicators
		if(i < 50) {
			return;
		}
		
		
		
		try {
			//check if has position
			if(! order.HasPosition(product)) {
				state = 0;
			}
			
			//close positions if breakout 10 day low
			double rangeLow = low10.getRangeLow(i - 1);
			double low = bidTs.get(i).getLow();
			if(state > 0 && low < rangeLow) {
				// close position
				Position p = order.getPosition(product);
				order.MarketSell(product, bidTs.get(i).getStart(), rangeLow - 0.0001, p.getAmount());
				
				// cancel all pending orders
				order.CancelAllPendingOrders(product);				
				state = 0;
				//System.out.println(String.format("breakout 10 day low. market sell %d at %f. cancel all pending.", p.getAmount(), low));
			}
			
			
			//check for open new positions
			double rangeHigh = high20.getRangeHigh(i - 1);
			double high = bidTs.get(i).getHigh();
			
			if(state == 0 && high > rangeHigh) {
				// buy one unit
				n = this.atr.getAtr(i - 1);
				double dollarVol = n * 10000 * order.getAccount().getDollarPerPoint();
				unit = (int) Math.floor(0.01 * order.getAccount().getBalance() / dollarVol);
				//System.out.println("Unit:" + unit);
				String entryTime = askTs.get(i).getStart();
				entryPrice = rangeHigh + 0.0001;
				double stopPrice = entryPrice - 2 * n;
			
				order.MarketBuy(product, entryTime, entryPrice, unit);
				order.StopSell(product, entryTime, stopPrice, unit);
				state = 1;
				
				//System.out.println(String.format("n:%f. breakout 20 day high. market buy %d at %f. stop at %f", n, unit, entryPrice, stopPrice));
			}
			
			if(state > 0) {
				while(state < 4 && high > entryPrice + n /2) {
					//add one unit until 4 units
					String entryTime = askTs.get(i).getStart();
					entryPrice = entryPrice + n/2;
					order.MarketBuy(product, entryTime, entryPrice, unit);
					
					//raise stops for earlier units
					List<PendingOrder> list = order.getStopSellOrders(product);
					for(int k=0; k < list.size(); k++) {
						PendingOrder po = list.get(k);
						double stopPrice = po.getPrice() + n/2;
						order.UpdatePendingOrder(po, po.getAmount(), stopPrice);
						//System.out.println(String.format("adjust stop price to %f", stopPrice));
					}
					
					state++;
					//System.out.println(String.format("adding unit. market buy %d at %f", unit, entryPrice));
				}
				}
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}
	

}
