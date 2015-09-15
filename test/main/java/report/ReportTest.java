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

		// create default account with balance $5000
		this.account = new Account(5000);
		this.session.save(account);
		tx.commit();
    }

    @After
    public void tearDown() {
        this.session.close();
        this.session = null;
    }

	@Test
	public void test1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.20, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 200.0, 0.01);
	}

	@Test
	public void test2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.20, 1);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 1);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 300.0, 0.01);
	}

	@Test
	public void test3() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 1);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.20, 1);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 300.0, 0.01);
	}

	@Test
	public void test4() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.20, 3);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 4);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.40, 1);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 800.0, 0.01);
	}

	@Test
	public void test5() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketSell("EURUSD", "2015-09-07 00:00:00", 1.20, 2);
        order.MarketBuy("EURUSD", "2015-09-08 00:00:00", 1.10, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 200.0, 0.01);
	}

	@Test
	public void test6() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.20, 1);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 1);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 300.0, 0.01);
	}

	@Test
	public void test7() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 2);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 1);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.20, 1);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 300.0, 0.01);
	}

	@Test
	public void test8() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.30, 4);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.40, 1);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.20, 3);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 800.0, 0.01);
	}

	@Test
	public void test9() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.10, 2);
        order.MarketSell("EURUSD", "2015-09-08 00:00:00", 1.00, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, -200.0, 0.01);
	}

    @Test
    public void test10() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("EURUSD", "2015-09-07 00:00:00", 1.00, 2);
        order.MarketBuy("GBPUSD", "2015-09-08 00:00:00", 2.00, 1);
        order.MarketBuy("GBPUSD", "2015-09-09 00:00:00", 3.00, 1);
        order.MarketSell("EURUSD", "2015-09-10 00:00:00", 2.00, 2);
        order.MarketSell("GBPUSD", "2015-09-10 00:00:00", 4.00, 2);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, 5000.0, 0.01);
    }

    @Test
    public void test11() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketSell("GBPUSD", "2015-09-09 00:00:00", 3.00, 1);
        order.MarketBuy("GBPUSD", "2015-09-10 00:00:00", 4.00, 1);
        order.MarketSell("GBPUSD", "2015-09-11 00:00:00", 4.00, 1);
        order.MarketBuy("GBPUSD", "2015-09-12 00:00:00", 5.00, 1);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, -2000.0, 0.01);
    }

    @Test
    public void test12() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        order.MarketBuy("GBPUSD", "2015-09-09 00:00:00", 4.00, 1);
        order.MarketSell("GBPUSD", "2015-09-10 00:00:00", 3.00, 1);
        order.MarketSell("GBPUSD", "2015-09-11 00:00:00", 4.00, 1);
        order.MarketBuy("GBPUSD", "2015-09-12 00:00:00", 5.00, 1);
        Report report = new Report(this.session, this.account);
        double totalPl = report.getProfitLoss();
        assertEquals(totalPl, -2000.0, 0.01);
    }
}

