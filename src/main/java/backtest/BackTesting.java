package main.java.backtest;
import main.java.data.*;
import main.java.strategy.*;
import main.java.model.*;
import main.java.order.*;
import main.java.report.*;
import org.hibernate.*;
import org.hibernate.cfg.*;
import java.util.*;

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
		Strategy strategy = new EmaCrossStrategy(order);

		MarketDataPusher mdp = new MarketDataPusher("EURUSD", 5, "2015-08-01", "2015-09-01");
		int barNum = mdp.getBarNum();

		// attach order as subscriber for market data
		mdp.AttachOrder(order);
		// attach strategy as subscriber for market data
		mdp.AttachStrategy(strategy);

		int i = 0;
		// push market data and run strategy
		while(true) {
			i++;

			//System.out.println(String.format("%d/%d", i, barNum));
			boolean flag = mdp.Notify();
			if(flag == false) {
				break;
			}
		}

		// generate profit loss report
		try {
			Report report = new Report(session, account);
			double totalPl = report.getProfitLoss();
			System.out.println("Total P/L:" + totalPl);
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}

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
