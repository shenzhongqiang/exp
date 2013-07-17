import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarketDataPusher {
	private String product;
	private int curr;
	private int timeframe;
	private Date start;
	private Date end;
	private ArrayList<Strategy> subscribers;
	private ArrayList<MarketData> buffer;
	
	public MarketDataPusher(String product, int timeframe, 
			String start, String end) {
		this.product = product;
		this.timeframe = timeframe;
		
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		try {
			this.start = ft.parse(start);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse start time " + start);
		}
		
		try {
			this.end = ft.parse(end);
		}
		catch(ParseException e) {
			System.out.println("Unable to parse end time " + end);
		}
		
		if(this.start.compareTo(this.end) > 0) {
			return;
		}
		
		TreeMap<Date, String> tm = getDateFileMap();
		Set<Date> dates = tm.keySet();
		this.buffer = new ArrayList<MarketData>();
		for(Date date: dates) {
			String filePath = tm.get(date); 
			
			if(date.compareTo(this.start) >= 0 && date.compareTo(this.end) <= 0) {
				ArrayList<MarketData> md = readMarketData(product, filePath);
				this.buffer.addAll(md);
			}	
			
		}
		
		this.subscribers = new ArrayList<Strategy>();
		curr = 0;
	}
	
	public boolean Notify() {
		if(curr < this.buffer.size()) {
			MarketData md = this.buffer.get(curr);
			for(Strategy s: this.subscribers) {
				s.Update(md);
			}
			
			curr++;
			return true;
		}
		
		return false;
	}
	
	public void Attach(Strategy o) {
		this.subscribers.add(o);
	}
	
	public void Detach(Strategy o) {
		this.subscribers.remove(o);
	}
	
	private static ArrayList<MarketData> readMarketData(String product, String filePath) {
		String pattern = "(\\d{4}-\\d{2}-\\d{2})";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(filePath);
		
		String date = "";
		if(m.find()) {
			date = m.group(1);
		}
		
		ArrayList<MarketData> al = new ArrayList<MarketData>();
		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			String line;
			
			while((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				
				for(int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceAll("^\"|\"$", "");
				}
				String start = date + " " + parts[0];
				String end = date + " " + parts[1];
				
				MarketData md = new MarketData(product, start, end, 
						Float.parseFloat(parts[2]), 
						Float.parseFloat(parts[3]), 
						Float.parseFloat(parts[4]), 
						Float.parseFloat(parts[5]), 
						Float.parseFloat(parts[6]), 
						Float.parseFloat(parts[7]), 
						Float.parseFloat(parts[8]), 
						Float.parseFloat(parts[9]), 
						Integer.parseInt(parts[10]));
				al.add(md);
			}
			
			br.close();
			
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file " + filePath);
		}
		catch(IOException ex) {
			System.out.println("Error reading file " + filePath);
		}
		
		return al;
	}
	
	private static TreeMap<Date, String> getDateFileMap() {
		String path = "marketdata\\FX_FXCM_Demo_EUR-USD_2012_EST_15";
		File folder = new File(path);
		File[] files = folder.listFiles();
		String pattern = "(\\d{4}-\\d{2}-\\d{2})";
		Pattern r = Pattern.compile(pattern);
		TreeMap<Date, String> tm = new TreeMap<Date, String>();
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		
		for(int i = 0; i < files.length; i++) {
			if(files[i].isFile()) {
				String filePath = files[i].getPath();
				Matcher m = r.matcher(filePath);

				if(m.find()) {
					try {
						Date tradingDate = ft.parse(m.group(1));
						tm.put(tradingDate, filePath);
					}
					catch(ParseException e) {
						System.out.println("Unable to parse date using " + ft);
					}
					
				}
			}
		}
		return tm;
	}
}
