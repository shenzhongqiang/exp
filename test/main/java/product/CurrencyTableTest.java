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
        System.out.format("%f %f %f\n", point, valuePerPoint, marginPerMiniLot);
    }

    @Test(expected=InvalidCurrency.class)
    public void testInvalidCurrencyException() {
        double point = CurrencyTable.getPoint("EURJPY");
    }
}
