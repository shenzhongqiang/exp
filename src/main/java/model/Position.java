package main.java.model;
import java.util.*;

public class Position {
	private int id;
	private Account account;
	private String product;
	private int amount;
	private String type;
	private Date time;
	private double price;
	private Set<PendingOrder> pendingOrders = new HashSet<PendingOrder>(0);

	public Position() {

	}

	public Position(Account account, Date time,  String product, double price, int amount) {
		this.account = account;
		this.time = time;
		this.product = product;
		this.price = price;
		this.amount = amount;
		this.type = "";
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getProduct() {
		return this.product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public double getPrice() {
		return this.price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<PendingOrder> getPendingOrders() {
		return this.pendingOrders;
	}

	public void setPendingOrders(Set pendingOrders) {
		this.pendingOrders = pendingOrders;
	}
}
