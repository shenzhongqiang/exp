
public class TurtleStrategy extends Strategy {

	@Override
	public void Update(MarketData md) {
		marketData.add(md);
	}

}
