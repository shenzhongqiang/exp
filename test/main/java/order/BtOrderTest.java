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
	public void testMarketBuy1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), 0);
        assertEquals(th.getProfit(), 0, 0.001);
	}

	@Test
	public void testMarketBuy2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketSell(product, "2015-09-08 00:00:00", 1.20, 2);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 2);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), 2);
        assertEquals(th.getProfit(), 200, 0.0001);
	}

	@Test
	public void testMarketBuy3() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketSell(product, "2015-09-08 00:00:00", 1.20, 1);
        order.MarketSell(product, "2015-09-09 00:00:00", 1.30, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 3);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), 2);
        assertEquals(th.getProfit(), 300, 0.0001);
	}

	@Test
	public void testMarketBuy4() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        order.MarketBuy(product, "2015-09-08 00:00:00", 1.20, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketSell(product, "2015-09-09 00:00:00", 1.30, 3);
        order.MarketSell(product, "2015-09-10 00:00:00", 1.40, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 4);
        TransactionHistory th1 = (TransactionHistory) q.list().get(0);
        assertEquals(th1.getClosed(), 2);
        assertEquals(th1.getProfit(), 400, 0.0001);
        TransactionHistory th2 = (TransactionHistory) q.list().get(1);
        assertEquals(th2.getClosed(), 2);
        assertEquals(th2.getProfit(), 300, 0.0001);
	}

	@Test
	public void testMarketBuy5() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketBuy(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketSell(product, "2015-09-09 00:00:00", 1.20, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 2);
        TransactionHistory th1 = (TransactionHistory) q.list().get(0);
        assertEquals(th1.getClosed(), 1);
        assertEquals(th1.getProfit(), 100, 0.0001);
	}

	@Test
	public void testMarketSell1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketSell(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), -2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), 0);
        assertEquals(th.getProfit(), 0, 0.001);
	}

	@Test
	public void testMarketSell2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketSell(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketBuy(product, "2015-09-08 00:00:00", 1.20, 2);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 2);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), -2);
        assertEquals(th.getProfit(), -200, 0.0001);
	}

	@Test
	public void testMarketSell3() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketSell(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketBuy(product, "2015-09-08 00:00:00", 1.20, 1);
        order.MarketBuy(product, "2015-09-09 00:00:00", 1.30, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 3);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getClosed(), -2);
        assertEquals(th.getProfit(), -300, 0.0001);
	}

	@Test
	public void testMarketSell4() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketSell(product, "2015-09-07 00:00:00", 1.10, 2);
        order.MarketSell(product, "2015-09-08 00:00:00", 1.20, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketBuy(product, "2015-09-09 00:00:00", 1.30, 3);
        order.MarketBuy(product, "2015-09-10 00:00:00", 1.40, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 0);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 4);
        TransactionHistory th1 = (TransactionHistory) q.list().get(0);
        assertEquals(th1.getClosed(), -2);
        assertEquals(th1.getProfit(), -400, 0.0001);
        TransactionHistory th2 = (TransactionHistory) q.list().get(1);
        assertEquals(th2.getClosed(), -2);
        assertEquals(th2.getProfit(), -300, 0.0001);
	}

	@Test
	public void testMarketSell5() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";
        order.MarketSell(product, "2015-09-07 00:00:00", 1.10, 2);
        assertTrue(order.HasPosition("EURUSD"));
        order.MarketBuy(product, "2015-09-09 00:00:00", 1.20, 1);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 1);
        q = this.session.createQuery("from TransactionHistory where product = :product order by time asc");
        q.setParameter("product", product);
        assertEquals(q.list().size(), 2);
        TransactionHistory th1 = (TransactionHistory) q.list().get(0);
        assertEquals(th1.getClosed(), -1);
        assertEquals(th1.getProfit(), -100, 0.0001);
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

    @Test
    public void testStopBuy1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // buy at specified price
        order.StopBuy(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.15, 1.05, 1.30, 1.00, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.16, 1.06, 1.31, 1.01, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.20, 0.0001);
    }

    @Test
    public void testStopBuy2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // slippage
        order.StopBuy(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.25, 1.15, 1.40, 1.10, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.26, 1.16, 1.41, 1.11, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), 2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.26, 0.0001);
    }

    @Test
    public void testLimitSell1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // sell at specified price
        order.LimitSell(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.15, 1.05, 1.30, 1.00, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.16, 1.06, 1.31, 1.01, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), -2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.20, 0.0001);
    }

    @Test
    public void testLimitSell2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // slippage
        order.LimitSell(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.25, 1.15, 1.40, 1.10, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.26, 1.16, 1.41, 1.11, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), -2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.25, 0.0001);
    }

    @Test
    public void testStopSell1() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // sell at specified price
        order.StopSell(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.25, 1.15, 1.40, 1.10, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.26, 1.16, 1.41, 1.11, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), -2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.20, 0.0001);
    }

    @Test
    public void testStopSell2() throws Exception {
		Order order = new BtOrder(this.session, this.account);
        String product = "EURUSD";

        // slippage
        order.StopSell(product, "2015-09-07 00:00:00", 1.20, 2);
        assertFalse(order.HasPosition("EURUSD"));
        MarketData bid = new MarketData(product, "2015-09-08 00:00:00", 1.15, 1.05, 1.30, 1.00, 100);
        MarketData ask = new MarketData(product, "2015-09-08 00:00:00", 1.16, 1.06, 1.31, 1.01, 100);
        order.Update(product, bid, ask);
        Query q = this.session.createQuery("from Position where product = :product");
        q.setParameter("product", product);
        Position p = (Position) q.list().get(0);
        assertEquals(p.getAmount(), -2);
        q = this.session.createQuery("from TransactionHistory where product = :product");
        q.setParameter("product", product);
        TransactionHistory th = (TransactionHistory) q.list().get(0);
        assertEquals(th.getPrice(), 1.15, 0.0001);
    }
}
