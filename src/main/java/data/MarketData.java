package main.java.data;

import java.util.*;
import java.text.*;
import main.java.exceptions.*;

public class MarketData {
	private String product;
	private Date start;
	private double Open;
	private double High;
	private double Low;
	private double Close;
	private int volume;

	public MarketData(String product, String start,
			double Open, double Close, double High, double Low, int volume) {
        if(High < Close || High < Open) {
            throw new IllegalMarketData();
        }
        if(Low > Close || Low > Open) {
            throw new IllegalMarketData();
        }

		this.product = product;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			this.start = ft.parse(start);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse start time using " + ft);
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

    public Date getStartDate() {
        return this.start;
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

    public String toString() {
        return String.format("<MarketData [%s] Start=%s Open=%f Close=%f High=%f Low=%f Volume=%d>",
            this.product, this.getStart(), this.Open, this.Close, this.High, this.Low, this.volume);
    }
}
