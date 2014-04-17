package main.java.strategy;

import java.util.*;

import main.java.data.MarketData;
import main.java.model.*;
import main.java.order.Order;
import main.java.subscriber.Subscriber;

public abstract class Strategy implements Subscriber {
	protected Order order;
	protected ArrayList<MarketData> askTs;
	protected ArrayList<MarketData> bidTs;
}
