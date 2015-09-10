package main.java.report;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;
import main.java.model.*;
import main.java.order.*;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class ReportTest {
    private Session session;
    private Account account;

    @Before
    public void setUp() {
		this.session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		// create default account with balance $5000
		this.account = new Account(5000);
		session.save(account);
		tx.commit();
    }

    @After
    public void tearDown() {
		Transaction tx = this.session.beginTransaction();
		// delete all existing positions
		Query q = this.session.createQuery("delete from Position");
		q.executeUpdate();
		// delete all existing transaction history
		q = this.session.createQuery("delete from TransactionHistory");
		q.executeUpdate();
		// delete all existing pending orders
		q = this.session.createQuery("delete from PendingOrder");
		q.executeUpdate();
		// delete all existing accounts
		q = this.session.createQuery("delete from Account");
		q.executeUpdate();
		tx.commit();
    }

	@Test
	public void test() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.20, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        System.out.println(totalPl);
	}

}

