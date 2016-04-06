package main.java.strategy;

import java.util.*;
import java.text.*;

import main.java.data.MarketData;
import main.java.indicator.*;
import main.java.model.*;
import main.java.subscriber.*;
import main.java.order.Order;
import main.java.product.*;

/**
 * EMA Cross Strategy
 *
 * @author Zhongqiang Shen
 */
public class BBStrategy extends Strategy implements Subscriber {
	// state
	// 0 - no open position
	// 1 - has position

	private int state = 0;
	private double stopPrice = 0;
    private double entryPrice = 0;
	private double takeProfit = 0;
	private double r = 0;
	private int unit = 0;
	private BollingerBand bb;

	/**
	 * Constructor
	 *
	 * @param order - the specified order {@link Order}
	 */
	public BBStrategy(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
		this.bb = new BollingerBand(20);
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
		bb.Update(bid);
		Run(product);
	}

	/**
	 * Run strategy
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 */
	public void Run(String product) {
		int i = bidTs.size() - 1;

		// skip first 100 market data to give room for calculating indicators
		if(i < 20) {
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
                /*
				if(day == 6 && hour == 16 && min >= 54 && hasPosition) {
					double bid = bidTs.get(i).getClose();
					order.MarketSell(product, start, bid, unit, this.positionId);
					order.CancelSellStopOrders(this.positionId);
					System.out.println("Sell on friday at " + bid);
					state = 0;
				}
                */
			}
			catch(ParseException e) {
				System.out.println("Unable to parse date using " + ft);
			}

            MarketData currBar = bidTs.get(i);
            MarketData prevBar = bidTs.get(i-1);
            double currBBUpper = bb.getUpperBand(i);
            double currBBLower = bb.getLowerBand(i);
            double currBBmiddle = bb.getMiddleBand(i);
            double prevBBUpper = bb.getUpperBand(i-1);
            double prevBBLower = bb.getLowerBand(i-1);
            double prevBBmiddle = bb.getMiddleBand(i-1);
            double bbWidth = currBBUpper - currBBLower;
            boolean longSignal = prevBar.getClose() < prevBBLower && currBar.getClose() > currBBLower && bbWidth < 0.002;
            boolean shortSignal = prevBar.getClose() > prevBBUpper && currBar.getClose() < currBBUpper && bbWidth < 0.002;

			if(state == 0) {
                if(!isLastBar(bidTs.get(i).getStartDate())) {
                    if(longSignal) {
                        this.entryPrice = askTs.get(i).getClose();
                        this.stopPrice = Math.min(bidTs.get(i).getLow(), bidTs.get(i-1).getLow()) - 0.0002;
                        this.stopPrice = entryPrice - 0.009;
                        String entryTime = askTs.get(i).getStart();
                        r = entryPrice - stopPrice;
                        takeProfit = currBBUpper;
                        double ratio = (takeProfit - entryPrice) / (entryPrice - stopPrice);
                        if(true) {
                            this.unit = this.getUnit(product, this.entryPrice, this.stopPrice);
                            order.MarketBuy(product, entryTime, entryPrice, this.unit);
                            order.StopSell(product, entryTime, stopPrice, this.unit);
                            state = 1;
                        }
                    }
                    if(shortSignal) {
                        this.entryPrice = bidTs.get(i).getClose();
                        this.stopPrice = Math.max(askTs.get(i).getHigh(), askTs.get(i-1).getHigh()) + 0.0002;
                        this.stopPrice = entryPrice + 0.009;
                        String entryTime = bidTs.get(i).getStart();
                        r = entryPrice - stopPrice;
                        takeProfit = currBBLower;
                        double ratio = (takeProfit - entryPrice) / (entryPrice - stopPrice);
                        if(true) {
                            this.unit = this.getUnit(product, this.entryPrice, this.stopPrice);
                            order.MarketSell(product, entryTime, entryPrice, this.unit);
                            order.StopBuy(product, entryTime, stopPrice, this.unit);
                            state = 1;
                        }
                    }
                }
			}
			else if(state == 1) {
				String exitTime = bidTs.get(i).getStart();
                boolean closeLongSignal = bidTs.get(i).getClose() > currBBUpper;
                boolean closeShortSignal = bidTs.get(i).getClose() < currBBLower;
                double bid = bidTs.get(i).getClose();
                double ask = askTs.get(i).getClose();

                if(isLastBar(bidTs.get(i).getStartDate())) {
                    if(r > 0) {
                        order.MarketSell(product, exitTime, bid, this.unit);
                    }
                    else {
                        order.MarketBuy(product, exitTime, ask, this.unit);
                    }
					order.CancelAllPendingOrders(product);
                    state = 0;
                }
				else if(r > 0 && closeLongSignal) {
                    order.MarketSell(product, exitTime, bid, this.unit);
					order.CancelAllPendingOrders(product);
                    state = 0;
				}
				else if(r < 0 && closeShortSignal) {
                    order.MarketBuy(product, exitTime, ask, this.unit);
					order.CancelAllPendingOrders(product);
                    state = 0;
				}
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}

    private int getUnit(String product, double entryPrice, double stopPrice) {
        double r = Math.abs(entryPrice - stopPrice);
        double point = CurrencyTable.getPoint(product);
        double valuePerPoint = CurrencyTable.getValuePerPoint(product);
        double balance = order.getAccount().getBalance();
        int unit = (int) (0.01 * balance / valuePerPoint / (r/point));
        return unit;
    }
}

