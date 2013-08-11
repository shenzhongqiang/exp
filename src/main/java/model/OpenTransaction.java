package model;

import java.util.Date;

public class OpenTransaction {
	private int id;
	private Account account;
	private String product;
	private String type;
	private double price;
	private int amount;
	private Date time;
	
	
	public OpenTransaction() {
		
	}
	
	public OpenTransaction(Account account, Date time, String product, double price, int amount) {
		this.account = account;
		this.product = product;
		this.price = price;
		this.amount = amount;
		this.time = time;
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
	
	public double getPrice() {
		return this.price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
}
