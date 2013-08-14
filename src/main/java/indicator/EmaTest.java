package indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import data.*;

public class EmaTest {

	@Test
	public void test() throws Exception {
		Ema ema = new Ema(5);
		ArrayList<MarketData> al = new ArrayList<MarketData>();
		al.add(new MarketData("EURUSD", "2013-01-01 09:00:00", "2013-01-01 09:15:00", 1, 2, 4, 0, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:15:00", "2013-01-01 09:30:00", 2, 3, 5, 1, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:30:00", "2013-01-01 09:45:00", 3, 4, 5, 2, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:45:00", "2013-01-01 10:00:00", 4, 5, 6, 3, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:00:00", "2013-01-01 10:15:00", 5, 6, 7, 4, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:15:00", "2013-01-01 10:30:00", 6, 7, 8, 4, 0));
		for(int i=0; i<6; i++) {
			double a = ema.getEma(i);
			System.out.println(i + "," + a);
		}
	}

}
