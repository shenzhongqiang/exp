package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import java.io.*;
import main.java.data.*;
import main.java.exceptions.*;

public class StandardDeviationTest {
	@Test
	public void test() throws Exception {
        StandardDeviation sd  = new StandardDeviation(5);
        MarketData md1 = new MarketData("EURUSD", "2015-09-01 00:00:00",
            1.1100, 1.1110, 1.1150, 1.1000, 1000);
        MarketData md2 = new MarketData("EURUSD", "2015-09-01 00:10:00",
            1.1100, 1.1111, 1.1150, 1.1000, 1000);
        MarketData md3 = new MarketData("EURUSD", "2015-09-01 00:20:00",
            1.1100, 1.1112, 1.1150, 1.1000, 1000);
        MarketData md4 = new MarketData("EURUSD", "2015-09-01 00:30:00",
            1.1100, 1.1113, 1.1150, 1.1000, 1000);
        MarketData md5 = new MarketData("EURUSD", "2015-09-01 00:40:00",
            1.1100, 1.1114, 1.1150, 1.1000, 1000);
        MarketData md6 = new MarketData("EURUSD", "2015-09-01 00:50:00",
            1.1100, 1.1115, 1.1150, 1.1000, 1000);
        sd.Update(md1);
        sd.Update(md2);
        sd.Update(md3);
        sd.Update(md4);
        sd.Update(md5);
        sd.Update(md6);
        assertEquals(sd.getStandardDeviation(5), 0.000158113, 0.000001);
	}
}


