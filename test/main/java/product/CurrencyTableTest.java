package main.java.product;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.util.*;
import main.java.exceptions.InvalidCurrency;

public class CurrencyTableTest {
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetInfo() {
        double point = CurrencyTable.getPoint("EURUSD");
        double valuePerPoint = CurrencyTable.getValuePerPoint("EURUSD");
        double marginPerMiniLot = CurrencyTable.getMarginPerMiniLot("EURUSD");
        assertEquals(point, 0.0001, 0.00001);
        assertEquals(valuePerPoint, 0.1, 0.00001);
        assertEquals(marginPerMiniLot, 3.25, 0.00001);
    }

    @Test(expected=InvalidCurrency.class)
    public void testInvalidCurrencyException() {
        double point = CurrencyTable.getPoint("EURJPY");
    }
}
