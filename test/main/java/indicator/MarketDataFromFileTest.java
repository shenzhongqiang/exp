package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import java.io.*;
import main.java.data.*;
import main.java.exceptions.*;

public class MarketDataFromFileTest {
	@Test
	public void test1() throws Exception {
        File historyFile = new File("src/main/java/history/EURUSDD1");
        MarketDataFromFile mdff = new MarketDataFromFile("EURUSD", 1440, historyFile);
        MarketData data1 = new MarketData("EURUSD",
            "2015-02-26 06:00:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        mdff.Update(data1);
        ArrayList<MarketData> series = mdff.getMarketData();
        MarketData currData = series.get(series.size()-1);
        MarketData prevData = series.get(series.size()-2);

        assertEquals(currData.getHigh(), 1.13895, 0.000001);
        assertEquals(currData.getLow(), 1.13364, 0.000001);
        assertEquals(currData.getOpen(), 1.13422, 0.000001);
        assertEquals(currData.getClose(), 1.13619, 0.000001);
        assertEquals(prevData.getHigh(), 1.13589, 0.000001);
        assertEquals(prevData.getLow(), 1.12890, 0.000001);
        assertEquals(prevData.getOpen(), 1.13353, 0.000001);
        assertEquals(prevData.getClose(), 1.13422, 0.000001);
	}
}

