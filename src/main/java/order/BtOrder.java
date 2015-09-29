package main.java.order;

import java.text.SimpleDateFormat;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.text.ParseException;
import main.java.model.*;
import main.java.data.*;
import main.java.exceptions.*;
import main.java.report.*;

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
		q.setParameter("product", product);

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
	 * @param amount - amount to buy (positive number)
	 * @return position id
	 */
	public int MarketBuy(String product, String strTime, double price, int amount) {
		try {
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = ft.parse(strTime);
			Transaction tx = this.session.beginTransaction();
            Query q = session.createQuery("from Position where product = :product");
            q.setParameter("product", product);
            List list = q.list();
            Position p = null;
            if(list.size() == 0) { // open new position
                p = new Position(this.account, product, amount);
                session.save(p);
                this.OpenBuyTransaction(time, product, price, amount);
            }
            else if(list.size() == 1) { // add to existing position or close position
                p = (Position) list.get(0);
                int totalAmount = p.getAmount() + amount;
                if(p.getAmount() > 0) { // add to existing position
                    p.setAmount(totalAmount);
                    session.update(p);
                    this.OpenBuyTransaction(time, product, price, amount);
                }
                else if(p.getAmount() < 0) { // close existing position
                    if(totalAmount > 0) {
                        throw new CloseMoreThanOpened();
                    }
                    else if(totalAmount == 0) {
                        session.delete(p);
                        this.CloseBuyTransaction(time, product, price, amount);
                    }
                    else {
                        p.setAmount(totalAmount);
                        session.update(p);
                        this.CloseBuyTransaction(time, product, price, amount);
                    }
                }
            }
            else {
                throw new MultiplePositions(product);
            }
			tx.commit();
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
	 * Open short position - open market sell order
	 *
	 * @param product - product to sell (e.g. EURUSD)
     * @param strTime - time of when the order is submitted
     * @param price - the bid price to sell
	 * @param amount - amount to sell, positive number
	 * @return position id
	 */
	public int MarketSell(String product, String strTime, double price, int amount) {
		try {
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = ft.parse(strTime);
			Transaction tx = this.session.beginTransaction();
            Query q = session.createQuery("from Position where product = :product");
            q.setParameter("product", product);
            List list = q.list();
            Position p = null;
            if(list.size() == 0) {
                p = new Position(this.account, product, amount*-1);
                session.save(p);
                this.OpenSellTransaction(time, product, price, amount);
            }
            else if(list.size() == 1) { // add to existing position or close position
                p = (Position) list.get(0);
                int totalAmount = p.getAmount() - amount;
                if(p.getAmount() < 0) { // add to existing position
                    p.setAmount(totalAmount);
                    session.update(p);
                    this.OpenSellTransaction(time, product, price, amount);
                }
                else if(p.getAmount() > 0) { // close existing position
                    if(totalAmount < 0) {
                        throw new CloseMoreThanOpened();
                    }
                    else if(totalAmount == 0) {
                        session.delete(p);
                        this.CloseSellTransaction(time, product, price, amount);
                    }
                    else {
                        p.setAmount(totalAmount);
                        session.update(p);
                        this.CloseSellTransaction(time, product, price, amount);
                    }
                }
            }
            else {
                throw new MultiplePositions(product);
            }
			tx.commit();
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
	 * Save buy transaction
	 *
	 * @param strTime - time of when the transaction is saved
	 * @param product - product to buy (e.g. EURUSD)
	 * @param price - the bid price to buy
	 * @param amount - amount to buy
	private void SaveBuyTransaction(String strTime, String product, double price, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);

			TransactionHistory th = new TransactionHistory(account, time, product, price, amount);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(th);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	 */

	/**
	 * Open buy transaction
	 *
	 * @param time - time of when the transaction is saved
	 * @param product - product to buy (e.g. EURUSD)
	 * @param price - the bid price to buy
	 * @param amount - amount to buy, positive number
	 */
	private void OpenBuyTransaction(Date time, String product, double price, int amount) {
        TransactionHistory th = new TransactionHistory(account, time, product, price, amount, 0, 0);
        session.save(th);
	}

	/**
	 * Close buy, buy to cover transaction
	 *
	 * @param time - time of when the transaction is saved
	 * @param product - product to buy (e.g. EURUSD)
	 * @param price - the bid price to buy
	 * @param amount - amount to buy, positive number
	 */
	private void CloseBuyTransaction(Date time, String product, double price, int amount) {
		Query q = session.createQuery("from TransactionHistory where product = :product and amount < 0 and closed > amount order by time asc");
		q.setParameter("product", product);
		List<TransactionHistory> list = q.list();
        if(list.size() == 0) {
            throw new NoMatchedOpenTransaction();
        }
        int remainToClose = amount;
        for(TransactionHistory th: list) {
            int closed = th.getClosed();
            int openAmount = th.getAmount() - th.getClosed();
            double openPrice = th.getPrice();
            Date openTime = th.getTime();
            if(remainToClose+openAmount < 0) {
                ClosedTransaction ct = new ClosedTransaction(openTime, "buy", remainToClose*-1, product, openPrice, time, price);
                double profit = ct.getPl();
                th.setProfit(th.getProfit()+profit);
                th.setClosed(closed-remainToClose);
                remainToClose = 0;
                session.save(th);
                break;
            }
            else {
                ClosedTransaction ct = new ClosedTransaction(openTime, "buy", openAmount, product, openPrice, time, price);
                double profit = ct.getPl();
                th.setProfit(th.getProfit()+profit);
                th.setClosed(th.getAmount());
                remainToClose += openAmount;
                session.save(th);
            }
        }

        TransactionHistory newTranx = new TransactionHistory(account, time, product, price, amount, amount, 0);
        session.save(newTranx);
	}

	/**
	 * Save sell transaction
	 *
	 * @param strTime - time of when the transaction is saved
	 * @param product - product to sell (e.g. EURUSD)
	 * @param price - the bid price to sell
	 * @param amount - amount to sell
	private void SaveSellTransaction(String strTime, String product, double price, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);

			TransactionHistory th = new TransactionHistory(account, time, product, price, amount);
			Transaction tx = this.session.beginTransaction();
			tx = session.beginTransaction();
			session.save(th);
			tx.commit();
		}
		catch(ParseException ex) {
			System.out.println("Error occurred when parsing " + strTime);
			ex.printStackTrace();
		}
	}
	 */

	/**
	 * Open sell transaction
	 *
	 * @param time - time of when the transaction is saved
	 * @param product - product to sell (e.g. EURUSD)
	 * @param price - the bid price to sell
	 * @param amount - amount to sell, positive number
	 */
	private void OpenSellTransaction(Date time, String product, double price, int amount) {
        TransactionHistory th = new TransactionHistory(account, time, product, price, amount*-1, 0, 0);
        session.save(th);
	}

	/**
	 * Close sell, sell to close long position
	 *
	 * @param time - time of when the transaction is saved
	 * @param product - product to sell (e.g. EURUSD)
	 * @param price - the bid price to sell
	 * @param amount - amount to sell, positive number
	 */
	private void CloseSellTransaction(Date time, String product, double price, int amount) {
		Query q = session.createQuery("from TransactionHistory where product = :product and amount > 0 and closed < amount order by time asc");
		q.setParameter("product", product);
		List<TransactionHistory> list = q.list();
        if(list.size() == 0) {
            throw new NoMatchedOpenTransaction();
        }
        int remainToClose = amount;
        for(TransactionHistory th: list) {
            int closed = th.getClosed();
            int openAmount = th.getAmount() - th.getClosed();
            double openPrice = th.getPrice();
            Date openTime = th.getTime();
            if(remainToClose <= openAmount) {
                ClosedTransaction ct = new ClosedTransaction(openTime, "sell", remainToClose, product, openPrice, time, price);
                double profit = ct.getPl();
                th.setProfit(th.getProfit()+profit);
                th.setClosed(closed+remainToClose);
                remainToClose = 0;
                session.save(th);
                break;
            }
            else {
                ClosedTransaction ct = new ClosedTransaction(openTime, "sell", openAmount, product, openPrice, time, price);
                double profit = ct.getPl();
                th.setProfit(th.getProfit()+profit);
                th.setClosed(th.getAmount());
                remainToClose -= openAmount;
                session.save(th);
            }
        }

        TransactionHistory newTranx = new TransactionHistory(account, time, product, price, amount*-1, amount*-1, 0);
        session.save(newTranx);
	}

	/**
	 * Open stop buy order
	 *
	 * @param product - product to buy (e.g. EURUSD)
	 * @param strTime - time of when the order is submitted
	 * @param stopPrice - the stop price to buy
	 */
	public void StopBuy(String product, String strTime, double stopPrice, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount, "stop");
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
	 */
	public void LimitBuy(String product, String strTime, double limitPrice, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount, "limit");
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
	 */
	public void StopSell(String product, String strTime, double stopPrice, int amount) {
		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, stopPrice, amount * -1, "stop");
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
	 */
	public void LimitSell(String product, String strTime, double limitPrice, int amount) {

		try {
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date time = ft.parse(strTime);
			PendingOrder po = new PendingOrder(this.account, time, product, limitPrice, amount * -1, "limit");
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
	 * Get pending orders of a product
	 *
	 * @param product - the specified product
	 * @return list {@link List} of pending orders
	 */
	public List getPendingOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where product = :product");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get stop buy orders of a product
	 *
	 * @param product - the specified product
	 * @return list {@link List} of stop buy orders
	 */
	public List getStopBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and product = :product and type = :type order by price asc");
		q.setParameter("type", "stop");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get limit buy orders of a product
	 *
	 * @param product - the specified product
	 * @return list {@link List} of limit buy orders
	 */
	public List getLimitBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount > 0 and product = :product and type = :type order by price desc");
		q.setParameter("type", "limit");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get stop sell orders of a product
	 *
	 * @param product - the specifed product
	 * @return list {@link List} of stop sell orders
	 */
	public List getStopSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and product = :product and type = :type order by price desc");
		q.setParameter("type", "stop");
		q.setParameter("product", product);
		List list = q.list();
		tx.commit();
		return list;
	}

	/**
	 * Get limit sell orders of a product
	 *
	 * @param product - the specified product
	 * @return list {@link List} of limit sell orders
	 */
	public List getLimitSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("from PendingOrder where amount < 0 and product = :product and type = :type order by price asc");
		q.setParameter("type", "limit");
		q.setParameter("product", product);
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
        this.DeletePendingOrder(po);
	}

	/**
	 * Delete the specified pending order
	 *
	 * @param po - the specified pending order
	 */
	public void DeletePendingOrder(PendingOrder po) {
		Transaction tx = session.beginTransaction();
		session.delete(po);
		tx.commit();
	}


	/**
	 * Cancel all pending orders of the specified product
	 *
	 * @param product - the specified product
	 */
	public void CancelAllPendingOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and product = :product");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all stop sell orders of the specified product
	 *
	 * @param product - the specified product
	 */
	public void CancelStopSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and product = :product and amount < 0 and type ='stop'");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all limit sell orders of the specified product
	 *
	 * @param product - the specified product
	 */
	public void CancelLimitSellOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and product = :product and amount < 0 and type ='limit'");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all stop buy orders of the specified product 
	 *
	 * @param product - the specified product
	 */
	public void CancelStopBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and product = :product and amount > 0 and type ='stop'");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
		q.executeUpdate();
		tx.commit();
	}

	/**
	 * Cancel all limit buy orders of the specified product
	 *
	 * @param product - the specified product
	 */
	public void CancelLimitBuyOrders(String product) {
		Transaction tx = session.beginTransaction();
		Query q = session.createQuery("delete from PendingOrder where account.id = :account_id and product = :product and amount > 0 and type ='limit'");
		q.setParameter("account_id", account.getId());
		q.setParameter("product", product);
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
		try {
            this.UpdateLimitBuy(product, bid, ask);
            this.UpdateStopBuy(product, bid, ask);
            this.UpdateLimitSell(product, bid, ask);
            this.UpdateStopSell(product, bid, ask);
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}

	}

	/**
	 * When new market data comes in, check for limit buy pending orders to see if any actions need to be taken
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
	public void UpdateLimitBuy(String product, MarketData bid, MarketData ask) {
        List<PendingOrder> list = this.getLimitBuyOrders(product);

        for(PendingOrder po: list) {
            if(ask.getLow() <= po.getPrice()) {
                double price = 0;
                if(ask.getOpen() <= po.getPrice()) {
                    price = ask.getOpen();
                }
                else {
                    price = po.getPrice();
                }
                this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
                this.DeletePendingOrder(po);
                //System.out.println("limit buy ===> market buy");
            }
        }
    }

	/**
	 * When new market data comes in, check for stop buy pending orders to see if any actions need to be taken
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
	public void UpdateStopBuy(String product, MarketData bid, MarketData ask) {
        List<PendingOrder> list = this.getStopBuyOrders(product);
        for(PendingOrder po: list) {
            if(ask.getHigh() >= po.getPrice()) {
                double price = 0;
                if(ask.getOpen() >= po.getPrice()) {
                    price = ask.getOpen();
                }
                else {
                    price = po.getPrice();
                }
                this.MarketBuy(po.getProduct(), ask.getStart(), price, po.getAmount());
                this.DeletePendingOrder(po);
                //System.out.println("stop buy ===> market buy");
            }
        }
    }

	/**
	 * When new market data comes in, check for limit sell pending orders to see if any actions need to be taken
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
	public void UpdateLimitSell(String product, MarketData bid, MarketData ask) {
        List<PendingOrder> list = this.getLimitSellOrders(product);
        for(PendingOrder po: list) {
            if(bid.getHigh() >= po.getPrice()) {
                double price = 0;
                if(bid.getOpen() >= po.getPrice()) {
                    price = bid.getOpen();
                }
                else {
                    price = po.getPrice();
                }
                this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
                this.DeletePendingOrder(po);
                //System.out.println("limit sell ===> market sell");
            }
        }
    }

	/**
	 * When new market data comes in, check for stop sell pending orders to see if any actions need to be taken
	 *
	 * @param product - the specified product (e.g. EURUSD)
	 * @param bid - the bid {@link MarketData}
	 * @param ask - the ask {@link MarketData}
	 */
	public void UpdateStopSell(String product, MarketData bid, MarketData ask) {
        List<PendingOrder> list = this.getStopSellOrders(product);
        for(PendingOrder po: list) {
            if(bid.getLow() <= po.getPrice()) {
                double price = 0;
                if(bid.getOpen() <= po.getPrice()) {
                    price = bid.getOpen();
                }
                else {
                    price = po.getPrice();
                }
                this.MarketSell(po.getProduct(), bid.getStart(), price, po.getAmount() * -1);
                this.DeletePendingOrder(po);
                //System.out.println("stop sell ===> market sell");
            }
        }
    }
}
