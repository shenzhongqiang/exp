package report;
import java.util.*;
import java.io.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import model.*;
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
		// initialize queue to store profit/loss for every closed transaction
		ArrayList<Double> transacPl = new ArrayList<Double>();
		
		// initialize total P/L to 0
		double totalPl = 0;
		// initialize queue director to 0. 1 means long, -1 means short.
		int queueDir = 0;
		
		// create spreadsheet
		FileOutputStream out = new FileOutputStream("report.xls");
		Workbook wb = new HSSFWorkbook();
		Sheet s = wb.createSheet();
		wb.setSheetName(0, "Closed Transaction");
		writeHeader(wb, s);
		
		
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
								totalPl += pl;
								remainAmount += item.getAmount();
								j.remove();
								writeClosedTransaction(wb, s, item.getTime(), "buy", item.getAmount(), item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the short transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								item.setAmount(remainAmount + item.getAmount());
								writeClosedTransaction(wb, s, item.getTime(), "buy", remainAmount, item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
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
								totalPl += pl;
								remainAmount += item.getAmount();
								j.remove();
								writeClosedTransaction(wb, s, item.getTime(), "sell", item.getAmount(), item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the long transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								item.setAmount(remainAmount + item.getAmount());
								writeClosedTransaction(wb, s, item.getTime(), "sell", remainAmount, item.getProduct(), item.getPrice(), th.getTime(), th.getPrice(), pl);
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
	
		adjustColumnWidth(wb, s);
		wb.write(out);
		out.close();
		return totalPl;
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
		for(int i = 0; i < r.getLastCellNum(); i++) {
			Cell c = r.getCell(i);
			c.setCellStyle(styleHeader);
		}
		
	}
	
	public static void writeClosedTransaction(Workbook wb, Sheet s, 
			Date openTime, String type, int size, String product, 
			double openPrice, Date closeTime, double closePrice, double profit) {
		int rowEnd = s.getLastRowNum();
		Row r = s.createRow((short)rowEnd + 1);
		CreationHelper createHelper = wb.getCreationHelper();
		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
		Cell cellOpenTime = r.createCell(0);
		cellOpenTime.setCellValue(openTime);
		cellOpenTime.setCellStyle(dateStyle);
		r.createCell(1).setCellValue(type);
		r.createCell(2).setCellValue(size);
		r.createCell(3).setCellValue(product);
		r.createCell(4).setCellValue(openPrice);
		Cell cellCloseTime = r.createCell(5);
		cellCloseTime.setCellValue(closeTime);
		cellCloseTime.setCellStyle(dateStyle);
		r.createCell(6).setCellValue(closePrice);
		r.createCell(7).setCellValue(profit);
	}
	
	public static void adjustColumnWidth(Workbook wb, Sheet s) {
		Row r = s.getRow(0);
		for(int i = 0; i < r.getLastCellNum(); i++) {
			s.autoSizeColumn(i);
		}
	}
}
