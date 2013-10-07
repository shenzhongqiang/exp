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
	public abstract Position getPosition(int positionId);
	public abstract int MarketBuy(String product, String time, double price, int amount);
	public abstract int MarketBuy(String product, String time, double price, int amount, int positionId);
	public abstract int MarketSell(String product, String time, double price, int amount);
	public abstract int MarketSell(String product, String time, double price, int amount, int positionId);
	public abstract void StopBuy(String product, String time, double stopPrice, int amount, int positionId);
	public abstract void LimitBuy(String product, String time, double limitPrice, int amount, int positionId);
	public abstract void StopSell(String product, String time, double stopPrice, int amount, int positionId);
	public abstract void LimitSell(String product, String time, double limitPrice, int amount, int positionId);
	public abstract List getPendingOrders(int positionId);
	public abstract List getStopBuyOrders(int positionId);
	public abstract List getLimitBuyOrders(int positionId);
	public abstract List getStopSellOrders(int positionId);
	public abstract List getLimitSellOrders(int positionId);
	public abstract void UpdatePendingOrder(PendingOrder po, int amount, double price);
	public abstract void CancelPendingOrder(PendingOrder po);
	public abstract void CancelAllPendingOrders(int positionId);
	public abstract void CancelSellStopOrders(int positionId);
	public abstract void CancelSellLimitOrders(int positionId);
	
	public Account getAccount() {
		return this.account;
	}
}
