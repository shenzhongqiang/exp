
public class BackTesting {

	public static void main(String[] args) {
		MarketDataPusher mdp = new MarketDataPusher("EURUSD", 15, "2012-01-01", "2012-12-31");
		while(mdp.Notify()) {
			
		}
	}

}
