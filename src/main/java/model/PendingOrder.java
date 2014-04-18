package main.java.model;

import java.util.Date;

public class PendingOrder {
	private int id;
	private Account account;
	private String product;
	private int amount;
	private double price;
	private String type;
	private Date time;
	private Position position;

	public PendingOrder() {

	}

	public PendingOrder(Account account, Date time, String product, double price, int amount, String type) {
		this.account = account;
		this.time = time;
		this.product = product;
		this.price = price;
		this.amount = amount;
		this.type = type;
	}

	public PendingOrder(Account account, Date time, String product, double price, int amount, String type, Position position) {
		this(account, time, product, price, amount, type);
		this.position = position;
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

	public String getProduct() {
		return this.product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getPrice() {
		return this.price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Position getPosition() {
		return this.position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
}
