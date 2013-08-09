import data.*;
import strategy.*;
import model.*;

import org.hibernate.*;
import org.hibernate.cfg.*;

import java.util.*;

import order.*;
import report.*;

public class BackTesting {

	public static void main(String[] args) {
		// Default account is account with id 1
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		Account account = (Account) session.get(Account.class, 1);
		tx.commit();
		
		// clean up existing open positions, pending orders, transactions
		cleanUp(session, account);
		
		// create new order object and turtle strategy object
		Order order = new BtOrder(session, account);
		TurtleStrategy ts = new TurtleStrategy(order);
		
		MarketDataPusher mdp = new MarketDataPusher("EURUSD", 15, "2012-12-15", "2012-12-31");
		int barNum = mdp.getBarNum();
		
		// attach order as subscriber for market data
		mdp.AttachOrder(order);
		// attach strategy as subscriber for market data
		mdp.AttachStrategy(ts);
		
		int i = 0;
		// push market data and run strategy
		while(true) {
			i++;
			System.out.println(String.format("%d/%d", i, barNum));
			boolean flag = mdp.Notify();
			if(flag == false) {
				break;
			}
		}
		
		// generate profit loss report
		Report report = new Report(session, account);
		double totalPl = report.getProfitLoss();		
		System.out.println(totalPl);
		
		// close database connection
		session.close();
		HibernateUtil.shutdown();
	}
	
	/**
	 * Remove all open positions, all pending orders and all transactions from database
	 * 
	 * @param session - hibernate session, which is used to interact with database
	 * @param account - the specified account {@link Account}
	 */
	public static void cleanUp(Session session, Account account) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from Position where account.id = :id");
		q.setParameter("id", account.getId());
		q.executeUpdate();
		q = session.createQuery("delete from TransactionHistory where account.id = :id");
		q.setParameter("id", account.getId());
		q.executeUpdate();
		q = session.createQuery("delete from PendingOrder where account.id = :id");
		q.setParameter("id", account.getId());
		q.executeUpdate();
		tx.commit();
	}
}
