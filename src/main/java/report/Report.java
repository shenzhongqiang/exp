package main.java.report;
import java.util.*;
import java.io.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import main.java.model.*;
import java.text.SimpleDateFormat;

/**
 * Report can be used to generate profit/loss report for a specified account.
 * 
 * @author Zhongqiang Shen
 */
public class Report {
	private Session session;
	private Account account;
	
	/**
	 * Constructor
	 * 
	 * @param session - hibernate session used to interact with database
	 * @param account - the account {@link Account} to generate report for
	 */
	public Report(Session session, Account account) {
		this.session = session;
		this.account = account;
	}
	
	/**
	 * Get profit/loss
	 * 
	 * @return profit/loss
	 */
	public double getProfitLoss() throws Exception {
		// get all transaction histories from database
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from TransactionHistory where account.id = :id and product = :product order by time asc");
		q.setParameter("id",  1);
		q.setParameter("product", "EURUSD");
		List<TransactionHistory> list = q.list();
		tx.commit();
		
		// initialize FIFO queue to empty queue. The queue will be used to store open transactions
		ArrayList<TransactionHistory> open = new ArrayList<TransactionHistory>();
		// array to store closed transactions
		ArrayList<ClosedTransaction> closed = new ArrayList<ClosedTransaction>();
		// initialize queue director to 0. 1 means long, -1 means short.
		int queueDir = 0;
		
		// calculate total profit loss
		for(int k = 0; k < list.size(); k++) {
			// get transaction item
			TransactionHistory th = list.get(k);
			Date time = th.getTime();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strTime =  ft.format(time);
			
			if(open.size() == 0) {
				// if queue is empty, push transaction item into the queue
				open.add(th);
				// set queue direction to the direction of the transaction item
				queueDir = th.getAmount() > 0 ? 1 : -1;
			}
			else {
				// if queue is not empty, push transaction item into the queue only when
				// transaction item has the same direction of the queue,
				// otherwise close transaction items in the queue.
				int dir = th.getAmount() > 0 ? 1: -1;
				if(dir == queueDir) {
					// if same direction, push into queue
					open.add(th);
				}
				else {
					// if opposite direction, close transactions in the queue and calculate P/L
					if(queueDir == 1) {
						// if queue direction is long, which means transaction item's direction is short
						// use the short transaction to close long transactions in the queue
						int remainAmount = th.getAmount();
						Iterator<TransactionHistory> j = open.iterator();
						while(j.hasNext() && remainAmount < 0) {
							TransactionHistory item = j.next();
							if(remainAmount + item.getAmount() <= 0) {
								// if there is still remaining amount in the short transaction 
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								remainAmount += item.getAmount();
								j.remove();
								ClosedTransaction ct = new ClosedTransaction(item.getTime(),"buy", item.getAmount(), item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								closed.add(ct);
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the short transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								item.setAmount(remainAmount + item.getAmount());
								ClosedTransaction ct = new ClosedTransaction(item.getTime(), "buy", remainAmount, item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								closed.add(ct);
								remainAmount = 0;
								
								//System.out.println(String.format("%s - close %d, profit %f", strTime, remainAmount, pl));
							}
						}
						
						// if after closing all long transactions in the queue, 
						// there is still remaining amount in the short transaction,
						// push that short transaction with remaining amount to the queue
						if(remainAmount < 0) {
							th.setAmount(remainAmount);
							open.add(th);
						}
					}
					else {
						// if queue direction is short, which means transaction item's direction is long
						// use the long transaction to close short transactions in the queue
						int remainAmount = th.getAmount();
						Iterator<TransactionHistory> j = open.iterator();
						while(j.hasNext() && remainAmount > 0) {
							TransactionHistory item = j.next();
							if(remainAmount + item.getAmount() >= 0) {
								// if there is still remaining amount in the long transaction 
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								remainAmount += item.getAmount();
								j.remove();
								ClosedTransaction ct = new ClosedTransaction(item.getTime(), "sell", item.getAmount(), item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								closed.add(ct);
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the long transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								item.setAmount(remainAmount + item.getAmount());
								ClosedTransaction ct = new ClosedTransaction(item.getTime(), "sell", remainAmount, item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								closed.add(ct);
								remainAmount = 0;
								
								//System.out.println(String.format("%s - close %d, profit %f", strTime, remainAmount, pl));
							}
						}
						
						// if after closing all short transactions in the queue, 
						// there is still remaining amount in the long transaction,
						// push that long transaction with remaining amount to the queue
						if(remainAmount > 0) {
							th.setAmount(remainAmount);
							open.add(th);
						}
					}
				}
			}
		}
		
		double totalPl = writeReportToExcel(closed);
		return totalPl;
	}
	
	
	public static double writeReportToExcel(ArrayList<ClosedTransaction> closed) {
		try {
			// create spreadsheet
			FileOutputStream out = new FileOutputStream("report.xls");
			Workbook wb = new HSSFWorkbook();
			Sheet s = wb.createSheet();
			wb.setSheetName(0, "Closed Transaction");
			writeHeader(wb, s);
			
			double totalPl = 0;
			Iterator<ClosedTransaction> i = closed.iterator();
			while(i.hasNext()) {
				ClosedTransaction ct = i.next();
				totalPl += ct.getPl();
				writeClosedTransaction(wb, s, ct, totalPl);
			}
			
			writeProfitLossDetails(wb, s, closed);
			adjustColumnWidth(wb, s);
			wb.write(out);
			out.close();
			return totalPl;
		}
		catch(FileNotFoundException ex) {
			System.out.println("file not found");
		}
		catch(IOException ex) {
			System.out.println("write failed");
		}
		return 0;
	}
	
	public static void writeHeader(Workbook wb, Sheet s) {
		// Create cell style for header row
		CellStyle styleHeader = wb.createCellStyle();
		Font fontHeader = wb.createFont();
		fontHeader.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleHeader.setFont(fontHeader);
		Row r = s.createRow((short)0);
		r.createCell(0).setCellValue("Open Time");
		r.createCell(1).setCellValue("Type");
		r.createCell(2).setCellValue("Size");
		r.createCell(3).setCellValue("Product");
		r.createCell(4).setCellValue("Open Price");
		r.createCell(5).setCellValue("Close Time");
		r.createCell(6).setCellValue("Close Price");
		r.createCell(7).setCellValue("Profit");
		r.createCell(8).setCellValue("Total Profit");
		for(int i = 0; i < r.getLastCellNum(); i++) {
			Cell c = r.getCell(i);
			c.setCellStyle(styleHeader);
		}
		
	}
	
	public static void writeClosedTransaction(Workbook wb, Sheet s, 
			ClosedTransaction ct, double totalPl) {
		int rowEnd = s.getLastRowNum();
		Row r = s.createRow((short)rowEnd + 1);
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
		Cell cellOpenTime = r.createCell(0);
		cellOpenTime.setCellValue(ct.getOpenTime());
		cellOpenTime.setCellStyle(dateStyle);
		r.createCell(1).setCellValue(ct.getType());
		r.createCell(2).setCellValue(ct.getSize());
		r.createCell(3).setCellValue(ct.getProduct());
		r.createCell(4).setCellValue(ct.getOpenPrice());
		Cell cellCloseTime = r.createCell(5);
		cellCloseTime.setCellValue(ct.getCloseTime());
		cellCloseTime.setCellStyle(dateStyle);
		r.createCell(6).setCellValue(ct.getClosePrice());
		r.createCell(7).setCellValue(ct.getPl());
		r.createCell(8).setCellValue(totalPl);
	}
	
	public static void adjustColumnWidth(Workbook wb, Sheet s) {
		Row r = s.getRow(0);
		for(int i = 0; i < r.getLastCellNum(); i++) {
			s.autoSizeColumn(i);
		}
	}
	
	public static void writeProfitLossDetails(Workbook wb, Sheet s, ArrayList<ClosedTransaction> closed) {
		int rowEnd = s.getLastRowNum();
		s.createRow((short)rowEnd + 1);
		s.createRow((short)rowEnd + 2);
		
		Row r = s.createRow((short)rowEnd + 3);
		
		double grossProfit = getGrossProfit(closed);
		r.createCell(0).setCellValue("Gross Profit");
		r.createCell(1).setCellValue(grossProfit);
		r.createCell(2);
		
		double grossLoss = getGrossLoss(closed);
		r.createCell(3).setCellValue("Gross Loss");
		r.createCell(4).setCellValue(grossLoss);
		r.createCell(5);
		
		double totalPl = getTotalProfitLoss(closed);
		r.createCell(6).setCellValue("Total Net Profit");
		r.createCell(7).setCellValue(totalPl);
		
		r = s.createRow((short)rowEnd + 4);
		double profitFactor = grossLoss == 0? 0 : grossProfit / grossLoss;
		r.createCell(0).setCellValue("Profit Factor");
		r.createCell(1).setCellValue(profitFactor);
		
		r = s.createRow((short)rowEnd + 5);
		int totalTrades = closed.size();
		r.createCell(0).setCellValue("Total Trades");
		r.createCell(1).setCellValue(totalTrades);
		r.createCell(2);
		String shortTradesSummary = getShortTradesSummary(closed);
		r.createCell(3).setCellValue("Short Trades (won %)");
		r.createCell(4).setCellValue(shortTradesSummary);
		r.createCell(5);
		
		String longTradesSummary = getLongTradesSummary(closed);
		r.createCell(6).setCellValue("Long Trades (won %)");
		r.createCell(7).setCellValue(longTradesSummary);
		
		r = s.createRow((short)rowEnd + 6);
		int profitTrades = getProfitTrades(closed);
		r.createCell(3).setCellValue("Profit Trades");
		r.createCell(4).setCellValue(profitTrades);
		r.createCell(5);
		
		int lossTrades = getLossTrades(closed);
		r.createCell(6).setCellValue("Loss Trades");
		r.createCell(7).setCellValue(lossTrades);
		
		r = s.createRow((short)rowEnd + 7);
		double maxProfit = getMaxProfitTrade(closed);
		r.createCell(3).setCellValue("Largest Profit Trade");
		r.createCell(4).setCellValue(maxProfit);
		r.createCell(5);
		
		double maxLoss = getMaxLossTrade(closed);
		r.createCell(6).setCellValue("Largest Loss Trade");
		r.createCell(7).setCellValue(maxLoss);
		
		r = s.createRow((short)rowEnd + 8);
		double avgProfit = getAvgProfitTrade(closed);
		r.createCell(3).setCellValue("Average Profit Trade");
		r.createCell(4).setCellValue(avgProfit);
		r.createCell(5);
		
		double avgLoss = getAvgLossTrade(closed);
		r.createCell(6).setCellValue("Average Loss Trade");
		r.createCell(7).setCellValue(avgLoss);
		
		r = s.createRow((short)rowEnd + 9);
		double maxDrawdown = getMaxDrawdown(closed);
		r.createCell(0).setCellValue("Maximal Drawdown");
		r.createCell(1).setCellValue(maxDrawdown);
	}
	
	
	public static double getTotalProfitLoss(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double total = 0;
		while(i.hasNext()) {
			total += i.next().getPl();
		}
		return total;
	}
	
	
	public static double getGrossProfit(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double total = 0;
		while(i.hasNext()) {
			double pl = i.next().getPl();
			if(pl >= 0) {
				total += pl;
			}
		}
		return total;
	}
	
	
	public static double getGrossLoss(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double total = 0;
		while(i.hasNext()) {
			double pl = i.next().getPl();
			if(pl < 0) {
				total += pl;
			}
		}
		return total * -1;
	}
	
	
	public static int getProfitTrades(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		int total = 0;
		while(i.hasNext()) {
			double pl = i.next().getPl();
			if(pl >= 0) {
				total += 1;
			}
		}
		return total;
	}
	
	
	public static int getLossTrades(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		int total = 0;
		while(i.hasNext()) {
			double pl = i.next().getPl();
			if(pl < 0) {
				total += 1;
			}
		}
		return total;
	}
	
	
	public static String getShortTradesSummary(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		int shortTrades = 0;
		int shortProfitTrades = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			String type = ct.getType();
			double pl = ct.getPl();
			if(type == "sell") {
				shortTrades++;
				if(pl > 0) {
					shortProfitTrades++;
				}
			}
		}
		
		double profitPercent = shortTrades == 0 ? 0 : shortProfitTrades * 100/shortTrades;
		String summary = String.format("%d (%.2f%%)", shortTrades, profitPercent);
		return summary;
	}
	
	
	public static String getLongTradesSummary(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		int longTrades = 0;
		int longProfitTrades = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			String type = ct.getType();
			double pl = ct.getPl();
			if(type == "buy") {
				longTrades++;
				if(pl > 0) {
					longProfitTrades++;
				}
			}
		}
		
		double profitPercent = longTrades == 0 ? 0 : longProfitTrades * 100/longTrades;
		String summary = String.format("%d (%.2f%%)", longTrades, profitPercent);
		return summary;
	}

	
	public static double getMaxProfitTrade(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double max = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			double pl = ct.getPl();
			if(pl > max) {
				max = pl;
			}
		}
		return max;
	}
	
	
	public static double getMaxLossTrade(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double min = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			double pl = ct.getPl();
			if(pl < min) {
				min = pl;
			}
		}
		return min;
	}
	
	
	public static double getAvgProfitTrade(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double count = 0;
		double total = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			double pl = ct.getPl();
			if(pl > 0) {
				total += ct.getPl();
				count ++;
			}
		}
		double avg = count == 0 ? 0 : total / count;
		return avg;
	}
	
	
	public static double getAvgLossTrade(ArrayList<ClosedTransaction> closed) {
		Iterator<ClosedTransaction> i = closed.iterator();
		double count = 0;
		double total = 0;
		while(i.hasNext()) {
			ClosedTransaction ct = i.next();
			double pl = ct.getPl();
			if(pl < 0) {
				total += ct.getPl();
				count ++;
			}
		}
		double avg = count == 0 ? 0 : total / count;
		return avg;
	}
	
	public static double getMaxDrawdown(ArrayList<ClosedTransaction> closed) {
		double[] min = new double[closed.size()];
		
		for(int k = 0; k < closed.size(); k++) {
			double pl = closed.get(k).getPl();
			if(k == 0) {
				min[k] = pl;
			}
			else {
				min[k] = Math.min(min[k-1] + pl,  pl);
			}
		}
		
		double maxDrawdown = 0;
		for(int k = 0; k < closed.size(); k++) {
			maxDrawdown = Math.min(maxDrawdown, min[k]);
		}
		return maxDrawdown;
	}
}
