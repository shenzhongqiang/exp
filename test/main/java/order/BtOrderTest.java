package main.java.order;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;
import org.hibernate.*;
import org.hibernate.cfg.*;
import main.java.model.*;
import main.java.data.*;

public class BtOrderTest {
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
	public void testMarketBuy() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
	}

    @Test
    public void testLimitBuy1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // buy at specified price
        order.LimitBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.20, 1.00, 1.20, 1.00, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.25, 1.05, 1.25, 1.05, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.10, 0.0001);

    }

    @Test
    public void testLimitBuy2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // slippage
        order.LimitBuy(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.15, 1.05, 1.20, 1.00, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.16, 1.06, 1.21, 1.01, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.16, 0.0001);
    }
}
