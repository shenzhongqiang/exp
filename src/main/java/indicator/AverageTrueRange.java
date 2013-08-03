package indicator;
import java.util.*;
import data.MarketData;;

public class AverageTrueRange extends Indicator {
	private int n;
	private ArrayList<Float> buffer;
	public AverageTrueRange(int n) {
		this.buffer = new ArrayList<Float>();
		this.n = n;
	}
	
	public double getAtr(ArrayList<MarketData> md, int i) throws Exception {
		int len = md.size();
		if(len == 0) {
			throw new Exception("array list is empty");
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range, max allowed is " + len);
		}
		
		double atr = 0;
		for(int k = 0; k < i; k++) {
			double tr = this.getTr(md, k);
			atr = ((this.n - 1) * atr + tr) / this.n;
		}
		return atr;
	}
	
	public double getTr(ArrayList<MarketData> md, int i) throws Exception {
		int len = md.size();
		if(len == 0) {
			throw new Exception("array list is empty");
		}
		
		if(i >= len) {
			throw new Exception(i + " is out of range, max allowed is " + len);
		}
		
		double tr = 0;
		if(i == 0) {
			MarketData curr = md.get(i);
			tr = curr.getHigh() - curr.getLow();
		}
		else {
			MarketData curr = md.get(i);
			MarketData prev = md.get(i - 1);
			double a1 = curr.getHigh() - curr.getLow();
			double a2 = curr.getHigh() - prev.getClose();
			double a3 = prev.getClose() - curr.getLow();
			tr = Math.max(a1, a2);
			tr = Math.max(tr, a3);
		}
		return tr;
	}
}
