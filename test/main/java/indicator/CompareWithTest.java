package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import java.io.*;
import main.java.data.*;
import main.java.exceptions.*;

public class CompareWithTest {
	@Test
	public void test() throws Exception {
        File historyFile = new File("src/main/java/history/GBPUSDm5");
        CompareWith cw = new CompareWith("GBPUSD", historyFile);
        MarketData data = new MarketData("EURUSD", "2015-09-01 00:10:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        cw.Update(data);
        MarketData md = cw.getMarketData();
        assertEquals(md.getProduct(), "GBPUSD");
	}

	@Test(expected=MissingMarketData.class)
	public void test2() throws Exception {
        File historyFile = new File("src/main/java/history/GBPUSDm5");
        CompareWith cw = new CompareWith("GBPUSD", historyFile);
        MarketData data = new MarketData("EURUSD", "2015-09-01 00:11:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        cw.Update(data);
	}
}

