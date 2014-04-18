package main.java.order;

import java.text.SimpleDateFormat;
import java.util.*;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import main.java.model.*;
import main.java.data.*;

import java.text.ParseException;

/**
 * Back-testing order.
 * Used to open market order or pending order.
 * Order, Position and Transaction info is stored in the database.
 *
 * @author Zhongqiang Shen
 */
public class BtOrder extends Order  {

	/**
	 * Constructor
	 *
	 * @param session - hibernate session, used to interact with database
	 * @param account - the account {@link Account} that the order actions are associated with
	 */
	public BtOrder(Session session, Account account) {
		this.session = session;
		this.account = account;
	}

	/**
	 * Check if there is open position
	 *
	 * @param product - product (e.g. EURUSD)
	 * @return true if has open position, otherwise false
	 */
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

	/**
	 * Get open positions of a specified product.
	 * For a certain product, there should be at most one open position.
	 *
	 * @param product - product (e.g. EURUSD)
	 * @return Position {@link Position} of that product
	 */
	public Position getPosition(String product) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :account_id and product = :product");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
		List list = q.list();
		Position p = null;
		if(list.size() > 0) {
			p = (Position) list.get(0);
		}
		tx.commit();
		return p;
	}

	/**
	 * Get position of the specified position id
	 *
	 * @param positionId - id of the position
	 */
	public Position getPosition(int positionId) {
		Transaction tx = this.session.beginTransaction();
		Query q = session.createQuery("from Position where account.id = :account_id and id = :positionId");
		q.setParameter("account_id", account.getId());
		q.setParameter("positionId", positionId);
		List list = q.list();
		Position p = null;
		if(list.size() > 0) {
			p = (Position) list.get(0);
		}
		tx.commit();
		return p;
	}

	/**
	 * Open long position - open market buy order
	 *
	 * @param product - product to buy (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param price - the ask price to buy
	 * @param amount - amount to buy
	 * @return position id
	 */
	public int MarketBuy(String product, String strTime, double price, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = new Position(this.account, time, product, price, amount);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(p);
			tx.commit();
			this.SaveBuyTransaction(strTime, product, price, amount, p.getId(), 0);
			System.out.println(String.format("%s - buy %d mini lot %s at %f", strTime, amount, product, price));
			return p.getId();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}

		return 0;
	}

	/**
	 * Close short position - open market buy order
	 *
	 * @param product - product to buy (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param price - the ask price to buy
	 * @param amount - amount to buy
	 * @param positionId - position to close
	 * @return position id
	 */
	public int MarketBuy(String product, String strTime, double price, int amount, int positionId) {
		Position p = this.getPosition(positionId);

		if(p != null) {
			int totalAmount = p.getAmount() + amount;
			if(totalAmount == 0) {
				Transaction tx = session.beginTransaction();
				session.delete(p);
				tx.commit();
			}
			else if (totalAmount < 0) {
				p.setAmount(totalAmount);
				Transaction tx = session.beginTransaction();
				session.update(p);
				tx.commit();
			}
			else {
				System.out.println("cannot close amount more than the amount opened");
			}
			double pl = (price - p.getPrice()) * 1000 * amount;
			this.SaveBuyTransaction(strTime, product, price, amount, positionId, pl);
			System.out.println(String.format("%s - buy %d mini lot %s at %f", strTime, amount, product, price));
			return positionId;
		}
		else {
			System.out.println("not found position");
		}

		return 0;
	}

	/**
	 * Open short position - open market sell order
	 *
	 * @param product - product to sell (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param price - the bid price to sell
	 * @param amount - amount to sell
	 * @return position id
	 */
	public int MarketSell(String product, String strTime, double price, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = new Position(this.account, time, product, price, amount * -1);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(p);
			tx.commit();
			this.SaveSellTransaction(strTime, product, price, amount, p.getId(), 0);
			System.out.println(String.format("%s - sell %d mini lot %s at %f", strTime, amount, product, price));
			return p.getId();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}

		return 0;
	}


	/**
	 * Close long position - open market sell order
	 *
	 * @param product - product to sell (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param price - the bid price to sell
	 * @param amount - amount to sell
	 * @param positionId - position to close
	 * @return position id
	 */
	public int MarketSell(String product, String strTime, double price, int amount, int positionId) {
		Position p = this.getPosition(positionId);

		if(p != null) {
			int totalAmount = p.getAmount() - amount;
			if(totalAmount == 0) {
				Transaction tx = session.beginTransaction();
				session.delete(p);
				tx.commit();
			}
			else if(totalAmount > 0){
				p.setAmount(totalAmount);
				Transaction tx = session.beginTransaction();
				session.update(p);
				tx.commit();
			}
			else {
				System.out.println("Cannot close amount more than the amount opened");
				return 0;
			}
			double pl = (price - p.getPrice()) * 1000 * amount;
			this.SaveSellTransaction(strTime, product, price, amount, positionId, pl);
			System.out.println(String.format("%s - sell %d mini lot %s at %f", strTime, amount, product, price));
			return positionId;
		}
		else {
			System.out.println("not found position");
		}

		return 0;
	}
	/**
	 * Save buy transaction
	 *
	 * @param strTime - time of when the transaction is saved
	 * @param product - product to buy (e.g. EURUSD)
	 * @param price - the bid price to buy
	 * @param amount - amount to buy
	 * @param positionId - id of position
	 * @param pl - profit/loss
	 */
	private void SaveBuyTransaction(String strTime, String product, double price, int amount, int positionId, double pl) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);

			TransactionHistory th = new TransactionHistory(account, time, product, price, amount, positionId, pl);
			account.setBalance(account.getBalance() + pl);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(th);
			session.update(account);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Save sell transaction
	 *
	 * @param strTime - time of when the transaction is saved
	 * @param product - product to sell (e.g. EURUSD)
	 * @param price - the bid price to sell
	 * @param amount - amount to sell
	 * @param positionId - id of position
	 * @param pl - profit/loss
	 */
	private void SaveSellTransaction(String strTime, String product, double price, int amount, int positionId, double pl) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);

			TransactionHistory th = new TransactionHistory(account, time, product, price, amount * -1, positionId, pl);
			account.setBalance(account.getBalance() + pl);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(th);
			session.update(account);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Open stop buy order
	 *
	 * @param product - product to buy (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param stopPrice - the stop price to buy
	 * @param positionId - id of position, 0 means open position, otherwise close position
	 */
	public void StopBuy(String product, String strTime, double stopPrice, int amount, int positionId) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = this.getPosition(positionId);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount, "stop", p);
			Transaction tx = session.beginTransaction();
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Open limit buy order
	 *
	 * @param product - product to buy (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param limitPrice - the limit price to buy
	 * @param amount - amount to buy
	 * @param positionId - id of position, 0 means open position, otherwise close position
	 */
	public void LimitBuy(String product, String strTime, double limitPrice, int amount, int positionId) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = this.getPosition(positionId);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount, "limit", p);
			Transaction tx = session.beginTransaction();
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Open stop sell order
	 *
	 * @param product - product to sell (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param stopPrice - the stop price to sell
	 * @param amount - amount to sell
	 * @param positionId - id of position, 0 means open position, otherwise close position
	 */
	public void StopSell(String product, String strTime, double stopPrice, int amount, int positionId) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = this.getPosition(positionId);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount * -1, "stop", p);
			Transaction tx = session.beginTransaction();
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Open limit sell order
	 *
	 * @param product - product to sell (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param limitPrice - the limit price to sell
	 * @param amount - amount to sell
	 * @param positionId - id of position, 0 means open position, otherwise close position
	 */
	public void LimitSell(String product, String strTime, double limitPrice, int amount, int positionId) {

		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			Position p = this.getPosition(positionId);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount * -1, "limit", p);
			Transaction tx = session.beginTransaction();
			session.save(po);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}

	/**
	 * Get pending orders of a position
	 *
	 * @param positionId - id of the position
	 * @return list {@link List} of pending orders
	 */
	public List getPendingOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where position.id = :positionId");
		q.setParameter("positionId", positionId);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get stop buy orders of a position
	 *
	 * @param positionId - id of position
	 * @return list {@link List} of stop buy orders
	 */
	public List getStopBuyOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and position.id = :positionId and type = :type");
		q.setParameter("type", "stop");
		q.setParameter("positionId", positionId);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get limit buy orders of a position
	 *
	 * @param positionId - id of position
	 * @return list {@link List} of limit buy orders
	 */
	public List getLimitBuyOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and position.id = :positionId and type = :type");
		q.setParameter("type", "limit");
		q.setParameter("positionId", positionId);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get stop sell orders of a position
	 *
	 * @param positionId - id of position
	 * @return list {@link List} of stop sell orders
	 */
	public List getStopSellOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and position.id = :positionId and type = :type");
		q.setParameter("type", "stop");
		q.setParameter("positionId", positionId);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get limit sell orders of a position
	 *
	 * @param positionId - id of position
	 * @return list {@link List} of limit sell orders
	 */
	public List getLimitSellOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and position.id = :positionId and type = :type");
		q.setParameter("type", "limit");
		q.setParameter("positionId", positionId);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Update amount and price of the specified pending order
	 *
	 * @param po - the specified pending order
	 * @param amount - new amount
	 * @param price - new price
	 */
	public void UpdatePendingOrder(PendingOrder po, int amount, double price) {
		Transaction tx = session.beginTransaction();
		po.setAmount(amount);
		po.setPrice(price);
		session.update(po);
		tx.commit();
	}

	/**
	 * Cancel the specified pending order
	 *
	 * @param po - the specified pending order
	 */
	public void CancelPendingOrder(PendingOrder po) {
		Transaction tx = session.beginTransaction();
		session.delete(po);
		tx.commit();
	}


	/**
	 * Cancel all pending orders of the specified position
	 *
	 * @param positionId - id of position
	 */
	public void CancelAllPendingOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and position.id = :positionId");
		q.setParameter("account_id", account.getId());
		q.setParameter("positionId", positionId);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all stop sell orders of the specified position
	 *
	 * @param positionId - id of position
	 */
	public void CancelSellStopOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and position.id = :positionId and amount < 0 and type ='stop'");
		q.setParameter("account_id", account.getId());
		q.setParameter("positionId", positionId);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all limit sell orders of the specified position
	 *
	 * @param positionId - id of position
	 */
	public void CancelSellLimitOrders(int positionId) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and position.id = :positionId and amount < 0 and type ='limit'");
		q.setParameter("account_id", account.getId());
		q.setParameter("positionId", positionId);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * When new market data comes in, check for all pending orders to see if any actions need to be taken
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
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
						if(po.getPosition() == null) {
							this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
						}
						else {
							this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount(), po.getPosition().getId());
						}
						this.CancelPendingOrder(po);
						//System.out.println("buy limit turns to market buy");
					}
				}
				else if(po.getAmount() > 0 && po.getType() == "stop") { //buy stop
					if(ask.getHigh() >= po.getPrice()) {
						double price = Math.max(ask.getLow(), po.getPrice());
						if(po.getPosition() == null) {
							this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
						}
						else {
							this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount(), po.getPosition().getId());
						}
						this.CancelPendingOrder(po);
						//System.out.println("buy stop turns to market buy");
					}
				}
				else if(po.getAmount() < 0 && po.getType() == "limit") { //sell limit
					if(bid.getHigh() >= po.getPrice()) {
						double price = Math.max(bid.getLow(), po.getPrice());
						if(po.getPosition() == null) {
							this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
						}
						else {
							this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1, po.getPosition().getId());
						}
						this.CancelPendingOrder(po);
						//System.out.println("sell limit turns to market sell");
					}
				}
				else if(po.getAmount() < 0 && po.getType() == "stop") { // sell stop
					if(bid.getLow() <= po.getPrice()) {
						double price = Math.min(bid.getHigh(), po.getPrice());
						if(po.getPosition() == null) {
							this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
						}
						else {
							this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1, po.getPosition().getId());
						}
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
