package strategy;

import java.util.*;

import data.MarketData;
import model.*;
import order.Order;
import subscriber.Subscriber;

public abstract class Strategy implements Subscriber {
	protected Order order;
	protected ArrayList<MarketData> askTs;
	protected ArrayList<MarketData> bidTs;
}
