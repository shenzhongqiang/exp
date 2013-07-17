import java.util.*;

public abstract class Strategy {
	protected ArrayList<MarketData> marketData;
	public abstract void Update(MarketData md);
	public Strategy() {
		marketData = new ArrayList<MarketData>();
	}
}
