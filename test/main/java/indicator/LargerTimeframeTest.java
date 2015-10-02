package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import main.java.data.*;
import main.java.exceptions.*;

public class LargerTimeframeTest {
	@Test
	public void test1() throws Exception {
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
            1.1400, 1.1410, 1.1450, 1.1300, 1300);
        MarketData data5 = new MarketData("EURUSD",
            "2015-09-01 00:40:00",
            1.1500, 1.1510, 1.1550, 1.1400, 1400);
        MarketData data6 = new MarketData("EURUSD",
            "2015-09-01 00:50:00",
            1.1600, 1.1610, 1.1650, 1.1500, 1500);
        MarketData data7 = new MarketData("EURUSD",
            "2015-09-01 01:00:00",
            1.1700, 1.1710, 1.1750, 1.1600, 1600);
        lt.Update(data1);
        lt.Update(data2);
        lt.Update(data3);
        lt.Update(data4);
        lt.Update(data5);
        lt.Update(data6);
        lt.Update(data7);
        MarketData md0 = lt.getMarketData().get(0);
        MarketData md1 = lt.getMarketData().get(1);

        assertEquals(md0.getHigh(), 1.1350, 0.000001);
        assertEquals(md0.getLow(), 1.1000, 0.000001);
        assertEquals(md0.getOpen(), 1.1100, 0.000001);
        assertEquals(md0.getClose(), 1.1310, 0.000001);
        assertEquals(md0.getVolume(), 3300);
        assertEquals(md1.getHigh(), 1.1650, 0.000001);
        assertEquals(md1.getLow(), 1.1300, 0.000001);
        assertEquals(md1.getOpen(), 1.1400, 0.000001);
        assertEquals(md1.getClose(), 1.1610, 0.000001);
        assertEquals(md1.getVolume(), 4200);
	}

	public void test2() throws Exception {
        LargerTimeframe lt = new LargerTimeframe(30);
        MarketData data1 = new MarketData("EURUSD",
            "2015-09-01 00:00:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        MarketData data2 = new MarketData("EURUSD",
            "2015-09-01 00:10:00",
            1.1200, 1.1210, 1.1250, 1.1100, 1100);
        MarketData data4 = new MarketData("EURUSD",
            "2015-09-01 00:30:00",
            1.1301, 1.1311, 1.1351, 1.1201, 1201);
        lt.Update(data1);
        lt.Update(data2);
        lt.Update(data4);
        MarketData md = lt.getMarketData().get(0);
        assertEquals(md.getHigh(), 1.1250, 0.000001);
        assertEquals(md.getLow(), 1.1000, 0.000001);
        assertEquals(md.getOpen(), 1.1100, 0.000001);
        assertEquals(md.getClose(), 1.1210, 0.000001);
        assertEquals(md.getVolume(), 2100);
	}

	@Test
	public void test3() throws Exception {
        LargerTimeframe lt = new LargerTimeframe(30);
        MarketData data = new MarketData("EURUSD",
            "2015-09-01 00:10:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        lt.Update(data);
        ArrayList<MarketData> md = lt.getMarketData();
        assertTrue(md.isEmpty());
	}
}
