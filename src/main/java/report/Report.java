package report;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import model.*;

public class Report {
	private Session session;
	private Account account;
	public Report(Session session, Account account) {
		this.session = session;
		this.account = account;
	}
	
	public double getPl() {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from TransactionHistory where account.id = :id order by time asc");
		q.setParameter("id",  1);
		List<TransactionHistory> list = q.list();
		tx.commit();
		
		ArrayList<TransactionHistory> open = new ArrayList<TransactionHistory>();
		double totalPl = 0;
		int queueDir = 0;
		for(int k = 0; k < list.size(); k++) {
			TransactionHistory th = list.get(k);
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
						for(int j = 0; j < open.size() && remainAmount < 0; j++) {
							TransactionHistory item = open.get(j);
							if(remainAmount + item.getAmount() <= 0) {
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount += item.getAmount();
								open.remove(item);
							}
							else {
								double pl = th.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount = 0;
								item.setAmount(th.getAmount() + item.getAmount());
							}
						}
						
						if(remainAmount < 0) {
							th.setAmount(remainAmount);
							open.add(th);
						}
					}
					else {
						int remainAmount = th.getAmount();
						for(int j = 0; j < open.size() && remainAmount > 0; j++) {
							TransactionHistory item = open.get(j);
							if(remainAmount + item.getAmount() >= 0) {
								double pl = item.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount += item.getAmount();
								open.remove(item);
							}
							else {
								double pl = th.getAmount() * 1000 * (th.getPrice() - item.getPrice());
								totalPl += pl;
								remainAmount = 0;
								item.setAmount(th.getAmount() + item.getAmount());
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
