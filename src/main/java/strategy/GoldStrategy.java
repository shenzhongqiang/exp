package main.java.strategy;

import java.util.*;
import main.java.data.MarketData;
import main.java.indicator.*;
import main.java.model.*;
import main.java.subscriber.*;
import main.java.order.Order;
import main.java.product.*;

/**
 * Turtle Strategy
 *
 * @author Zhongqiang Shen
 */
public class GoldStrategy extends Strategy {
	// state
	// 0 - no open position
	// 1 - one unit
	// 2 - two unit2
	// 3 - three units
	// 4 - four units
	private int state = 0;
	private double entryPrice = 0;
	private double stopPrice = 0;
    private double takeProfit = 0;
    private double r = 0;
	private int unit = 0;

	/**
	 * Constructor
	 *
	 * @param order - the specified order {@link Order}
	 */
	public GoldStrategy(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
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
		Run(product);
	}

	/**
	 * Run strategy
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 */
	public void Run(String product) {
		int i = bidTs.size() - 1;

		// skip first several bars to give room for calculating indicators
		if(i < 400) {
			return;
		}

        MarketData currBar = this.bidTs.get(i);
        MarketData prevBar = this.bidTs.get(i-1);
        if(! order.HasPosition(product)) {
            state = 0;
        }

        double currBody = currBar.getClose() - currBar.getOpen();
        double prevBody = prevBar.getClose() - prevBar.getOpen();
        double point = CurrencyTable.getPoint(product);
        if(state == 0) {
            boolean cond1 = prevBody > 0 && currBody < 0;
            boolean cond2 = currBar.getOpen() >= prevBar.getClose();
            boolean cond3 = currBar.getClose() <= prevBar.getOpen();
            boolean cond4 = currBody <= -3;
            if(cond1 && cond2 && cond3 && cond4) {
                String entryTime = bidTs.get(i).getStart();
                this.entryPrice = bidTs.get(i).getClose();
                order.MarketSell(product, entryTime,
                    entryPrice, 2);
                this.stopPrice = prevBar.getClose() + 100 * point;
                this.r = this.stopPrice - this.entryPrice;
                this.takeProfit = this.entryPrice - r;
                order.StopBuy(product, entryTime, this.stopPrice, 2);
                //System.out.format("entry:%f, sl:%f, tp:%f\n", entryPrice, stopPrice, takeProfit);
                state = 1;
            }
        }
        else if(state == 1) {
            if(this.askTs.get(i).getClose() < this.takeProfit) {
                order.MarketBuy(product, currBar.getStart(), this.takeProfit, 1);
                order.CancelAllPendingOrders(product);
                this.stopPrice = this.stopPrice - this.r;
                this.takeProfit = this.takeProfit - this.r;
                order.StopBuy(product, currBar.getStart(), this.stopPrice, 1);
                //System.out.format("cover at :%f\n", takeProfit);
                state = 2;
            }
        }
        else if(state == 2) {
            if(this.askTs.get(i).getClose() < this.takeProfit) {
                this.stopPrice = this.stopPrice - this.r;
                this.takeProfit = this.takeProfit - this.r;
                order.CancelAllPendingOrders(product);
                order.StopBuy(product, currBar.getStart(), this.stopPrice, 1);
            }
        }
	}


}
