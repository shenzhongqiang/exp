package report;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import model.*;
import java.text.SimpleDateFormat;

public class Report {
	private Session session;
	private Account account;
	public Report(Session session, Account account) {
		this.session = session;
		this.account = account;
	}
	
	public double getPl() {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from TransactionHistory where account.id = :id and product = :product order by time asc");
		q.setParameter("id",  1);
		q.setParameter("product", "EURUSD");
		List<TransactionHistory> list = q.list();
		tx.commit();
		
		ArrayList<TransactionHistory> open = new ArrayList<TransactionHistory>();
		double totalPl = 0;
		int queueDir = 0;
		for(int k = 0; k < list.size(); k++) {
			TransactionHistory th = list.get(k);
			Date time = th.getTime();
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strTime =  ft.format(time);
			if(open.size() == 0) {
				open.add(th);
				queueDir = th.getAmount() > 0 ? 1 : -1;
			}
			else {
				int dir = th.getAmount() > 0 ? 1: -1;
				if(dir == queueDir) {
					open.add(th);
				}
				else {
					if(queueDir == 1) {
						int remainAmount = th.getAmount();
						Iterator<TransactionHistory> j = open.iterator();
						while(j.hasNext() && remainAmount < 0) {
							TransactionHistory item = j.next();
							if(remainAmount + item.getAmount() <= 0) {
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount += item.getAmount();
								j.remove();
								System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								double pl = th.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount = 0;
								item.setAmount(th.getAmount() + item.getAmount());
								System.out.println(String.format("%s - close %d, profit %f", strTime, remainAmount, pl));
							}
						}
						
						if(remainAmount < 0) {
							th.setAmount(remainAmount);
							open.add(th);
						}
					}
					else {
						int remainAmount = th.getAmount();
						Iterator<TransactionHistory> j = open.iterator();
						while(j.hasNext() && remainAmount > 0) {
							TransactionHistory item = j.next();
							if(remainAmount + item.getAmount() >= 0) {
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount += item.getAmount();
								j.remove();
								System.out.println(String.format("%s - close %d, profit %f", strTime, item.getAmount(), pl));
							}
							else {
								double pl = th.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount = 0;
								item.setAmount(th.getAmount() + item.getAmount());
								System.out.println(String.format("%s - close %d, profit %f", strTime, remainAmount, pl));
							}
						}
						
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
