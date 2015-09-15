package main.java.report;

import java.text.SimpleDateFormat;
import java.util.*;
import main.java.product.*;

public class ClosedTransaction {
	private Date openTime;
	private String type;
	private int size; // size is in mini lot (i.e. 1K)
	private String product;
	private double openPrice;
	private Date closeTime;
	private double closePrice;
    private double pl;

	public ClosedTransaction(Date openTime, String type, int size, String product, double openPrice, Date closeTime, double closePrice) {
		this.openTime = openTime;
		this.type = type;
		this.size = size;
		this.product = product;
		this.openPrice = openPrice;
		this.closeTime = closeTime;
		this.closePrice = closePrice;

        double point = CurrencyTable.getPoint(product);
        double valuePerPoint = CurrencyTable.getValuePerPoint(product);
        this.pl = (closePrice - openPrice) * size * valuePerPoint / point;
	}

	public Date getOpenTime() {
		return this.openTime;
	}

	public String getType() {
		return this.type;
	}

	public int getSize() {
		return this.size;
	}

	public String getProduct() {
		return this.product;
	}

	public double getOpenPrice() {
		return this.openPrice;
	}

	public Date getCloseTime() {
		return this.closeTime;
	}

	public double getClosePrice() {
		return this.closePrice;
	}

    public double getPl() {
        return this.pl;
    }
}
