package main.java.indicator;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;
import main.java.data.*;

public class EmaTest {

	@Test
	public void test() throws Exception {
		Ema ema = new Ema(5);
		ArrayList<MarketData> al = new ArrayList<MarketData>();
		ema.Update(new MarketData("EURUSD", "2013-01-01 07:45:00", 1.11215, 1.11445, 1.11887, 1.10890, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 08:00:00", 1.11609, 1.11684, 1.11772, 1.11208, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 08:15:00", 1.11684, 1.12013, 1.12290, 1.11519, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 08:30:00", 1.12013, 1.12058, 1.12157, 1.11312, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 08:45:00", 1.12058, 1.12769, 1.12949, 1.11710, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 09:00:00", 1.12769, 1.13358, 1.13490, 1.12536, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 09:15:00", 1.13306, 1.13153, 1.13726, 1.12830, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 09:30:00", 1.13153, 1.12658, 1.13282, 1.12583, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 09:45:00", 1.12658, 1.12884, 1.13201, 1.12136, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 10:00:00", 1.12884, 1.14282, 1.14407, 1.12838, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 10:15:00", 1.14282, 1.12942, 1.14598, 1.12686, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 10:30:00", 1.12810, 1.11887, 1.13294, 1.11806, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 10:45:00", 1.11887, 1.11186, 1.12070, 1.11128, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 11:00:00", 1.11186, 1.11844, 1.12130, 1.11047, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 11:15:00", 1.11844, 1.12288, 1.12954, 1.11639, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 11:30:00", 1.12288, 1.11906, 1.12332, 1.11156, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 11:45:00", 1.11819, 1.12438, 1.12472, 1.11456, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 12:00:00", 1.12438, 1.12460, 1.12807, 1.11933, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 12:15:00", 1.12460, 1.11751, 1.12608, 1.11568, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 12:30:00", 1.11751, 1.11922, 1.12087, 1.11344, 0));
		ema.Update(new MarketData("EURUSD", "2013-01-01 12:45:00", 1.11922, 1.11763, 1.12034, 1.11733, 0));
	    assertEquals(ema.getEma(20), 1.1195, 0.0001);
	}

    @Test
	public void test2() throws Exception {
		Ema ema = new Ema(5);
		ArrayList<MarketData> al = new ArrayList<MarketData>();
		al.add(new MarketData("EURUSD", "2013-01-01 07:45:00", 1.11215, 1.11445, 1.11887, 1.10890, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 08:00:00", 1.11609, 1.11684, 1.11772, 1.11208, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 08:15:00", 1.11684, 1.12013, 1.12290, 1.11519, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 08:30:00", 1.12013, 1.12058, 1.12157, 1.11312, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 08:45:00", 1.12058, 1.12769, 1.12949, 1.11710, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:00:00", 1.12769, 1.13358, 1.13490, 1.12536, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:15:00", 1.13306, 1.13153, 1.13726, 1.12830, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:30:00", 1.13153, 1.12658, 1.13282, 1.12583, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 09:45:00", 1.12658, 1.12884, 1.13201, 1.12136, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:00:00", 1.12884, 1.14282, 1.14407, 1.12838, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:15:00", 1.14282, 1.12942, 1.14598, 1.12686, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:30:00", 1.12810, 1.11887, 1.13294, 1.11806, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 10:45:00", 1.11887, 1.11186, 1.12070, 1.11128, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 11:00:00", 1.11186, 1.11844, 1.12130, 1.11047, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 11:15:00", 1.11844, 1.12288, 1.12954, 1.11639, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 11:30:00", 1.12288, 1.11906, 1.12332, 1.11156, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 11:45:00", 1.11819, 1.12438, 1.12472, 1.11456, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 12:00:00", 1.12438, 1.12460, 1.12807, 1.11933, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 12:15:00", 1.12460, 1.11751, 1.12608, 1.11568, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 12:30:00", 1.11751, 1.11922, 1.12087, 1.11344, 0));
		al.add(new MarketData("EURUSD", "2013-01-01 12:45:00", 1.11922, 1.11763, 1.12034, 1.11733, 0));
        ArrayList<Double> emaData = ema.getEma(al);
        assertEquals(emaData.get(emaData.size()-1), 1.1195, 0.0001);
	}
}
