package report;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
	public double getProfitLoss() {
		// get all transaction histories from database
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from TransactionHistory where account.id = :id and product = :product order by time asc");
		q.setParameter("id",  1);
		q.setParameter("product", "EURUSD");
		List<TransactionHistory> list = q.list();
		tx.commit();
		
		// initialize FIFO queue to empty queue. The queue will be used to store open transactions
		ArrayList<TransactionHistory> open = new ArrayList<TransactionHistory>();
		// initialize total P/L to 0
		double totalPl = 0;
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
								totalPl += pl;
								remainAmount += item.getAmount();
								j.remove();
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the short transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								item.setAmount(remainAmount + item.getAmount());
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
								//System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								// if there is no remaining amount in the long transaction
								double pl = remainAmount * -1 * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount = 0;
								item.setAmount(remainAmount + item.getAmount());
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
			
		return totalPl;
	}
}
