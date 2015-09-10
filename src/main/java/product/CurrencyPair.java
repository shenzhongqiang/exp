package main.java.product;

import java.util.*;

public class CurrencyPair {
    private String name;
    private int miniLot = 1000;
    private double point;
    private double marginPerMiniLot;
    private double valuePerPoint;

    /**
     * Constructor
     *
     * @param name - the name of the currency pair
     * @param point - value of point
     * @param marginPerMiniLot - margin used by each mini lot
     * @param valuePerPoint - value of the move of each point
     */
    public CurrencyPair(String name, double point, double marginPerMiniLot, double valuePerPoint) {
        this.name = name;
        this.point = point;
        this.marginPerMiniLot = marginPerMiniLot;
        this.valuePerPoint = valuePerPoint;
    }

    public double getPoint() {
        return this.point;
    }

    public double getMarginPerMiniLot() {
        return this.marginPerMiniLot;
    }

    public double getValuePerPoint() {
        return this.valuePerPoint;
    }
}
