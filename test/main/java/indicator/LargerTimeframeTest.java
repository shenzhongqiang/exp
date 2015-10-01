package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import main.java.data.*;
import main.java.exceptions.*;

public class LargerTimeframeTest {
	@Test
	public void test() throws Exception {
        LargerTimeframe lt = new LargerTimeframe(30);
        MarketData data1 = new MarketData("EURUSD",
            "2015-09-01 00:00:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        MarketData data2 = new MarketData("EURUSD",
            "2015-09-01 00:10:00",
            1.1200, 1.1210, 1.1250, 1.1100, 1100);
        MarketData data3 = new MarketData("EURUSD",
            "2015-09-01 00:20:00",
            1.1300, 1.1310, 1.1350, 1.1200, 1200);
        MarketData data4 = new MarketData("EURUSD",
            "2015-09-01 00:30:00",
            1.1301, 1.1311, 1.1351, 1.1201, 1201);
        lt.Update(data1);
        lt.Update(data2);
        lt.Update(data3);
        lt.Update(data4);
        MarketData md = lt.getMarketData();
        assertEquals(md.getHigh(), 1.1350, 0.000001);
        assertEquals(md.getLow(), 1.1000, 0.000001);
        assertEquals(md.getOpen(), 1.1100, 0.000001);
        assertEquals(md.getClose(), 1.1310, 0.000001);
        assertEquals(md.getVolume(), 3300);
	}

	@Test(expected=NoMarketData.class)
	public void test2() throws Exception {
        LargerTimeframe lt = new LargerTimeframe(3);
        MarketData data = new MarketData("EURUSD",
            "2015-09-01 00:10:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        lt.Update(data);
        MarketData md = lt.getMarketData();
	}
}
