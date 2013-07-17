import java.util.*;
import java.text.*;

public class MarketData {
	private String product;
	private Date start;
	private Date end;
	private float bidOpen;
	private float bidHigh;
	private float bidLow;
	private float bidClose;
	private float askOpen;
	private float askHigh;
	private float askLow;
	private float askClose;
	private int volume;
	
	public MarketData(String product, String start, String end, 
			float bidOpen, float bidClose, float bidHigh, float bidLow, 
			float askOpen, float askClose, float askHigh, float askLow, 
			int volume
	) {
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
		
		this.bidOpen = bidOpen;
		this.bidHigh = bidHigh;
		this.bidLow = bidLow;
		this.bidClose = bidClose;
		this.askOpen = askOpen;
		this.askHigh = askHigh;
		this.askLow = askLow;
		this.askClose = askClose;
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
	
	public float getBidOpen() {
		return this.bidOpen;
	}
	
	public float getBidHigh() {
		return this.bidHigh;
	}
	
	public float getBidLow() {
		return this.bidLow;
	}
	
	public float getBidClose() {
		return this.bidClose;
	}
	
	public float getAskOpen() {
		return this.askOpen;
	}
	
	public float getAskHigh() {
		return this.askHigh;
	}
	
	public float getAskLow() {
		return this.askLow;
	}
	
	public float getAskClose() {
		return this.askClose;
	}
	
	public float getVolume() {
		return this.volume;
	}
	
}
