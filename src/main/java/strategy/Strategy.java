package strategy;

import java.util.*;
import data.MarketData;
import model.*;
import order.Order;

public abstract class Strategy {
	protected Order order;
	protected ArrayList<MarketData> askTs;
	protected ArrayList<MarketData> bidTs;
}
