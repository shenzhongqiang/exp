package subscriber;

import data.*;

public interface Subscriber {
	public void Update(String product, MarketData bid, MarketData ask);
}

