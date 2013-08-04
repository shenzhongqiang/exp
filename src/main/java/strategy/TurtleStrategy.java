package strategy;

import java.util.*;

import data.MarketData;
import indicator.*;
import model.*;
import subscriber.*;
import order.Order;

public class TurtleStrategy extends Strategy implements Subscriber {
	private int state = 0;
	private double entryPrice = 0;
	private double n = 0;
	private int unit = 0;
	public TurtleStrategy(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
	}
	
	@Override
	public void Update(String product, MarketData bid, MarketData ask) {
		bidTs.add(bid);
		askTs.add(ask);
		Run(product);
	}
	
	public void Run(String product) {
		int i = bidTs.size() - 1;
		if(i < 50) {
			return;
		}
		
		try {
			//check if has position
			if(! order.HasPosition(product)) {
				state = 0;
			}
			
			//close positions if breakout 10 day low
			double rangeLow = new HighLow(10).getLow(bidTs, i - 1);
			double low = bidTs.get(i).getLow();
			if(state > 0 && low < rangeLow) {
				// close position
				Position p = order.getPosition(product);
				order.MarketSell(product, bidTs.get(i).getStart(), low, p.getAmount());
				
				// cancel all pending orders
				order.CancelAllPendingOrders(product);				
				state = 0;
				//System.out.println("breakout 10 day low. market sell at " + low + ". cancel all pending.");
			}
			
			
			//check for open new positions
			double rangeHigh = new HighLow(20).getHigh(bidTs, i - 1);
			double high = bidTs.get(i).getHigh();
			
			if(state == 0 && high > rangeHigh) {
				// buy one unit
				AverageTrueRange atr = new AverageTrueRange(20);
				n = atr.getAtr(bidTs, i - 1);
				double dollarVol = n * 10000 * order.getAccount().getDollarPerPoint();
				unit = (int) Math.floor(0.01 * order.getAccount().getMargin() / dollarVol);
				String entryTime = askTs.get(i).getStart();
				entryPrice = askTs.get(i).getHigh();
				double stopPrice = entryPrice - 2 * n;
			
				order.MarketBuy(product, entryTime, entryPrice, unit);
				order.StopSell(product, entryTime, stopPrice, unit);
				state = 1;
				
				//System.out.println("breakout 20 day high. market buy at " + entryPrice + ". stop at " + stopPrice);
			}
			else if(state == 1 && state < 4 && high > entryPrice + n /2) {
				//add one unit until 4 units
				String entryTime = askTs.get(i).getStart();
				entryPrice = askTs.get(i).getHigh();
				order.MarketBuy(product, entryTime, entryPrice, unit);
				
				//raise stops for earlier units
				List<PendingOrder> list = order.getStopSellOrders(product);
				for(int k=0; k < list.size(); k++) {
					PendingOrder po = list.get(k);
					double stopPrice = po.getPrice() + n/2;
					order.UpdatePendingOrder(po, po.getAmount(), stopPrice);
				}
				
				state++;
				//System.out.println("adding unit. market buy at " + entryPrice);
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}
	

}
