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
public class TurtleStrategy extends Strategy {
	// state
	// 0 - no open position
	// 1 - one unit
	// 2 - two unit2
	// 3 - three units
	// 4 - four units
	private int state = 0;
	private double entryPrice = 0;
	private double stopPrice = 0;
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
	public TurtleStrategy(Order order) {
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
		high20.Update(ask);
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
            else {
                order.getPosition(product);
            }

			//close positions if breakout 10 day low
			double rangeLow = low10.getRangeLow(i - 1);
			double low = bidTs.get(i).getLow();
			if(state > 0 && low < rangeLow) {
				// close position
				Position p = order.getPosition(product);
				order.MarketSell(product, bidTs.get(i).getStart(), rangeLow, p.getAmount());

				// cancel all pending orders
				order.CancelAllPendingOrders(product);
				state = 0;
				System.out.println(String.format("breakout 10 day low. market sell %d at %f. cancel all pending.", p.getAmount(), rangeLow));
			}


			//check for open new positions
			double rangeHigh = high20.getRangeHigh(i - 1);
			double high = bidTs.get(i).getHigh();

			if(state == 0) {
                if(high > rangeHigh) {
                    // buy one unit
                    n = this.atr.getAtr(i - 1);
                    double point = CurrencyTable.getPoint(product);
                    double valuePerPoint = CurrencyTable.getValuePerPoint(product);
                    double dollarVol = n * valuePerPoint / point;
                    this.unit = (int) Math.floor(0.01 * order.getAccount().getBalance() / dollarVol);
                    String entryTime = askTs.get(i).getStart();
                    this.entryPrice = rangeHigh;
                    this.stopPrice = this.entryPrice - 2 * n;

                    order.MarketBuy(product, entryTime, this.entryPrice, this.unit);
                    order.StopSell(product, entryTime, this.stopPrice, this.unit);
                    state = 1;

                    System.out.format("state:%d, n:%f, ask:%f, bid:%f. breakout 20 day high. market buy %d at %f. stop at %f\n",
                        state, n, askTs.get(i).getClose(), bidTs.get(i).getClose(), unit, entryPrice, this.stopPrice);
                }
			}
			else if(state < 4) {
                if(high > this.entryPrice + n /2) {
                    //add one unit until 4 units
                    String entryTime = askTs.get(i).getStart();
                    this.entryPrice = this.entryPrice + n/2;
                    this.stopPrice = this.stopPrice + n/2;
                    order.MarketBuy(product, entryTime, this.entryPrice, this.unit);
                    order.StopSell(product, entryTime, this.stopPrice, this.unit);

                    //raise stops for earlier units
                    Position p = order.getPosition(product);
                    List<PendingOrder> list = order.getStopSellOrders(product);
                    for(int k=0; k < list.size(); k++) {
                        PendingOrder po = list.get(k);
                        order.UpdatePendingOrder(po, po.getAmount(), this.stopPrice);
                        //System.out.println(String.format("adjust stop price to %f", this.stopPrice));
                    }

                    state++;
                    System.out.format("state:%d, adding unit. market buy %d at %f\n", state, unit, this.entryPrice);
                }
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}


}
