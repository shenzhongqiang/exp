package main.java.data;

import java.util.*;
import java.text.*;

public class MarketData {
	private String product;
	private Date start;
	private Date end;
	private double Open;
	private double High;
	private double Low;
	private double Close;
	private int volume;

	public MarketData(String product, String start, String end,
			double Open, double Close, double High, double Low, int volume) {
		this.product = product;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.start = ft.parse(start);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse start time using " + ft);
		}

		try {
			this.end = ft.parse(end);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse end time using " + ft);
		}

		this.Open = Open;
		this.High = High;
		this.Low = Low;
		this.Close = Close;
		this.volume = volume;

	}

	public String getProduct() {
		return this.product;
	}

	public String getStart() {
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return ft.format(this.start);
	}

	public String getEnd() {
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return ft.format(this.end);
	}

	public double getOpen() {
		return this.Open;
	}

	public double getHigh() {
		return this.High;
	}

	public double getLow() {
		return this.Low;
	}

	public double getClose() {
		return this.Close;
	}

	public double getVolume() {
		return this.volume;
	}

}
