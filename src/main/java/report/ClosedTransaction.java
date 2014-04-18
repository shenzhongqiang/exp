package main.java.report;

import java.text.SimpleDateFormat;
import java.util.*;
public class ClosedTransaction {
	private Date openTime;
	private String type;
	private int size;
	private String product;
	private double openPrice;
	private Date closeTime;
	private double closePrice;
	private double pl;
	private static double totalPl = 0;

	public ClosedTransaction(Date openTime, String type, int size, String product, double openPrice, Date closeTime, double closePrice, double pl) {
		this.openTime = openTime;
		this.type = type;
		this.size = size;
		this.product = product;
		this.openPrice = openPrice;
		this.closeTime = closeTime;
		this.closePrice = closePrice;
		this.pl = pl;
		totalPl += pl;
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

	public double getTotalPl() {
		return totalPl;
	}
}
