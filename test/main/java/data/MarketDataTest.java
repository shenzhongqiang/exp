package main.java.data;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import main.java.data.*;
import org.joda.time.*;

public class MarketDataTest {
    @Test
    public void test() throws Exception {
        MarketData md = new MarketData("EURUSD",
            "2015-07-10 16:55:00", 1.11157, 1.11129, 1.11180, 1.11085, 86);
        DateTime dt = new DateTime(md.getStartDate());
        assertEquals(dt.getMonthOfYear(), 7);
        assertEquals(dt.getDayOfWeek(), 5);
        assertEquals(dt.getHourOfDay(), 16);
        assertEquals(dt.getMinuteOfHour(), 55);
    }

    @Test
    public void test2() throws Exception {
        MarketData md = new MarketData("EURUSD",
            "2015-07-11 04:55:00", 1.11157, 1.11129, 1.11180, 1.11085, 86);
        DateTime dt = new DateTime(md.getStartDate());
        assertEquals(dt.getDayOfWeek(), 6);
        assertEquals(dt.getHourOfDay(), 4);
        assertEquals(dt.getMinuteOfHour(), 55);
    }
}

