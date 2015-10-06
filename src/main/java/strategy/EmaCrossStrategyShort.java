package main.java.strategy;

import java.util.*;
import java.text.*;

import main.java.data.MarketData;
import main.java.indicator.*;
import main.java.model.*;
import main.java.subscriber.*;
import main.java.order.Order;

/**
 * EMA Cross Strategy
 *
 * @author Zhongqiang Shen
 */
public class EmaCrossStrategyShort extends Strategy implements Subscriber {
	// state
	// 0 - no open position
	// 1 - has position

	private int state = 0;
	private double stopPrice = 0;
    private double entryPrice = 0;
	private double takeProfit = 0;
	private double r = 0;
	private int unit = 0;
	private Ema ema10;
	private Ema ema20;
	private Ema ema200;
	private RangeLow low10;
	private RangeHigh high10;
	private int positionId = 0;

	/**
	 * Constructor
	 *
	 * @param order - the specified order {@link Order}
	 */
	public EmaCrossStrategyShort(Order order) {
		this.order = order;
		this.bidTs = new ArrayList<MarketData>();
		this.askTs = new ArrayList<MarketData>();
		this.ema10 = new Ema(10);
		this.ema20 = new Ema(20);
		this.ema200 = new Ema(200);
		this.low10 = new RangeLow(10);
		this.high10 = new RangeHigh(10);
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
		ema10.Update(bid);
		ema20.Update(bid);
		ema200.Update(bid);
		low10.Update(bid);
		high10.Update(bid);
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
		if(i < 300) {
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


			double prevEma10 = ema10.getEma(i-1);
			double prevEma20 = ema20.getEma(i-1);
            double prevEma200 = ema200.getEma(i-1);
			double currEma10 = ema10.getEma(i);
			double currEma20 = ema20.getEma(i);
			double currEma200 = ema200.getEma(i);
			double ask = askTs.get(i).getClose();
			double bid = bidTs.get(i).getClose();

			boolean crossedUp = prevEma10 < prevEma20 && currEma10 > currEma20;
			boolean crossedDown = prevEma10 > prevEma20 && currEma10 < currEma20;
			//boolean isUpTrend = currEma10 > currEma200 && prevEma20 > prevEma200;
			if(state == 0) {
                if(!isLastBar(bidTs.get(i).getStartDate()) && crossedDown) {
                    double rangeHigh = high10.getRangeHigh(i);

                    this.stopPrice = rangeHigh + 0.0002;
                    r = bid - stopPrice;
                    String entryTime = bidTs.get(i).getStart();
                    entryPrice = bid;
                    takeProfit = bid + r;
                    unit = 2;
                    this.positionId = order.MarketSell(product, entryTime, ask, unit);
                    order.StopBuy(product, entryTime, stopPrice, unit);
                    state = 1;
                    //System.out.println(String.format("r:%f, rangeLow:%f, market buy %d at %f. SL at %f.", r, rangeLow, unit, ask, stopPrice));
                }
			}
			else if(state == 1) {
				double high = bidTs.get(i).getHigh();
                double low = askTs.get(i).getLow();
				String exitTime = bidTs.get(i).getStart();

                if(isLastBar(bidTs.get(i).getStartDate())) {
                    order.MarketBuy(product, exitTime, ask, 2);
					order.CancelAllPendingOrders(product);
                    state = 0;
                }
                else if(crossedUp) {
                    order.MarketBuy(product, exitTime, ask, 2);
					order.CancelAllPendingOrders(product);
                    state = 0;
                }
			}

		}
		catch(Exception ex) {
			System.out.println(ex.getCause());
		}
	}
}
