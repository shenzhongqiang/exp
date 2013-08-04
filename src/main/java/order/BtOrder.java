package order;

import java.text.SimpleDateFormat;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import model.*;
import data.*;
import java.text.ParseException;

public class BtOrder extends Order  {
	public BtOrder(Session session, Account account) {
		this.session = session;
		this.account = account;
	}
	
	public boolean HasPosition(String product) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :id and product = :product");
		q.setParameter("id", account.getId());
		q.setParameter("product",product);
		
		List list = q.list();
		boolean hasPosition = false;
		if(list.size() > 0) {
			hasPosition = true;
		}
		tx.commit();
		return hasPosition;
	}
	
	public Position getPosition(String product) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :id and product = :product");
		q.setParameter("id", account.getId());
		q.setParameter("product", product);
		List list = q.list();
		Position p = null;
		if(list.size() > 0) {
			p = (Position) list.get(0);
		}
		tx.commit();
		return p;
	}
	
	public void MarketBuy(String product, String strTime, double price, int amount) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :id and product = :product");
		q.setParameter("id", account.getId());
		q.setParameter("product",product);
		List list = q.list();
		if(list.size() > 0) {
			Position p = (Position) list.get(0);
			int totalAmount = p.getAmount() + amount;
			if(totalAmount == 0) {
				session.delete(p);
			}
			else {
				p.setAmount(totalAmount);
				session.update(p);
			}
		}
		else {
			Position p = new Position(this.account, product, amount);
			session.save(p);
		}

		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			TransactionHistory th = new TransactionHistory(this.account, time, product, price, amount);
			session.save(th);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public void MarketSell(String product, String strTime, double price, int amount) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :id and product = :product");
		q.setParameter("id", account.getId());
		q.setParameter("product",product);
		List list = q.list();
		tx.commit();
		
		tx = session.beginTransaction();
		
		if(list.size() > 0) {
			Position p = (Position) list.get(0);
			int totalAmount = p.getAmount() - amount;
			if(totalAmount == 0) {
				session.delete(p);
			}
			else {
				p.setAmount(totalAmount);
				session.update(p);	
			}

		}
		else {
			Position p = new Position(this.account, product, amount * -1);
			session.save(p);
		}
		
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			TransactionHistory th = new TransactionHistory(this.account, time, product, price, amount * -1);
			session.save(th);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public void StopBuy(String product, String strTime, double stopPrice, int amount) {
		Transaction tx = session.beginTransaction();
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount, "stop");
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public void LimitBuy(String product, String strTime, double limitPrice, int amount) {
		Transaction tx = session.beginTransaction();
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount, "limit");
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public void StopSell(String product, String strTime, double stopPrice, int amount) {
		Transaction tx = session.beginTransaction();
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount * -1, "stop");
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public void LimitSell(String product, String strTime, double limitPrice, int amount) {
		Transaction tx = session.beginTransaction();
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount * -1, "limit");
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			tx.rollback();
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	
	public List getPendingOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where product = :product");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}
		
	public List getStopBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and product = :product and type = :type");
		q.setParameter("type", "stop");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}
	
	public List getLimitBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and product = :product and type = :type");
		q.setParameter("type", "limit");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}
	
	public List getStopSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and product = :product and type = :type");
		q.setParameter("type", "stop");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}
	
	public List getLimitSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and product = :product and type = :type");
		q.setParameter("type", "limit");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}
	
	public void UpdatePendingOrder(PendingOrder po, int amount, double price) {
		Transaction tx = session.beginTransaction();
		po.setAmount(amount);
		po.setPrice(price);
		session.update(po);
		tx.commit();
	}
	
	public void CancelPendingOrder(PendingOrder po) {
		Transaction tx = session.beginTransaction();
		session.delete(po);
		tx.commit();
	}
	
	public void CancelAllPendingOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where product = :product");
		q.setParameter("product", product);
		q.executeUpdate();
		tx.commit();
	}
	
	public void Update(String product, MarketData bid, MarketData ask) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where product = :product and account.id = :id");
		q.setParameter("id", account.getId());
		q.setParameter("product", product);
		List<PendingOrder> list = q.list();
		tx.commit();
	
		try {
			for(int k = 0; k < list.size(); k++) {
				PendingOrder po = list.get(k);
				
				if(po.getAmount() > 0 && po.getType() == "limit") { // buy limit
					if(ask.getLow() <= po.getPrice()) {
						double price = Math.min(ask.getHigh(), po.getPrice());
						this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
						this.CancelPendingOrder(po);
						//System.out.println("buy limit turns to market buy");
					}
				}
				else if(po.getAmount() > 0 && po.getType() == "stop") { //buy stop
					if(ask.getHigh() >= po.getPrice()) {
						double price = Math.max(ask.getLow(), po.getPrice());
						this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
						this.CancelPendingOrder(po);
						//System.out.println("buy stop turns to market buy");
					}	
				}
				else if(po.getAmount() < 0 && po.getType() == "limit") { //sell limit
					if(bid.getHigh() >= po.getPrice()) {
						double price = Math.max(bid.getLow(), po.getPrice());
						this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
						this.CancelPendingOrder(po);
						//System.out.println("sell limit turns to market sell");
					}
				}
				else if(po.getAmount() < 0 && po.getType() == "stop") { // sell stop
					if(bid.getLow() <= po.getPrice()) {
						double price = Math.min(bid.getHigh(), po.getPrice());
						this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
						this.CancelPendingOrder(po);
						//System.out.println("sell stop turns to market sell");
					}
				}
			}
			
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}
}
