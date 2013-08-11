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
		// initialize
		init(session);
		
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from Account");
		List l = q.list();
		if(l.size() == 0) {
			System.out.println("no account exists");
			System.exit(1);
		}
		
		Account account = (Account) l.get(0);
		tx.commit();
		
	
		// create new order object and turtle strategy object
		Order order = new BtOrder(session, account);
		TurtleStrategy ts = new TurtleStrategy(order);
		
		MarketDataPusher mdp = new MarketDataPusher("EURUSD", 15, "2012-01-01", "2012-03-31");
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
	public static void init(Session session) {
		Transaction tx = session.beginTransaction();
		// delete all existing positions
		Query q = session.createQuery("delete from Position");
		q.executeUpdate();
		// delete all open transactions
		q = session.createQuery("delete from OpenTransaction");
		q.executeUpdate();
		// delete all existing transaction history
		q = session.createQuery("delete from TransactionHistory");
		q.executeUpdate();
		// delete all existing pending orders
		q = session.createQuery("delete from PendingOrder");
		q.executeUpdate();
		// delete all existing accounts
		q = session.createQuery("delete from Account");
		q.executeUpdate();
		
		// create default account with balance $5000
		Account account = new Account(5000);
		session.save(account);
		tx.commit();
	}
}
