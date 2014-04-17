package main.java.subscriber;

import main.java.data.*;

public interface Subscriber {
	public void Update(String product, MarketData bid, MarketData ask);
}

