package order;

import model.*;
import java.util.*;
import subscriber.*;
import org.hibernate.Session;

public abstract class Order implements Subscriber {
	protected Session session;
	protected Account account;
	public abstract boolean HasPosition(String product);
	public abstract Position getPosition(String product);
	public abstract void MarketBuy(String product, String time, double price, int amount);
	public abstract void MarketSell(String product, String time, double price, int amount);
	public abstract void StopBuy(String product, String time, double stopPrice, int amount);
	public abstract void LimitBuy(String product, String time, double limitPrice, int amount);
	public abstract void StopSell(String product, String time, double stopPrice, int amount);
	public abstract void LimitSell(String product, String time, double limitPrice, int amount);
	public abstract List getPendingOrders(String product);
	public abstract List getStopBuyOrders(String product);
	public abstract List getLimitBuyOrders(String product);
	public abstract List getStopSellOrders(String product);
	public abstract List getLimitSellOrders(String product);
	public abstract void UpdatePendingOrder(PendingOrder po, int amount, double price);
	public abstract void CancelPendingOrder(PendingOrder po);
	public abstract void CancelAllPendingOrders(String product);
	
	public Account getAccount() {
		return this.account;
	}
}
