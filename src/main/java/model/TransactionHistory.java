package main.java.model;

import java.util.Date;
import java.text.SimpleDateFormat;

public class TransactionHistory {
	private int id;
	private Account account;
	private String product;
	private String type;
	private double price;
	private int amount;
	private Date time;
    private int closed;
    private double profit;

	public TransactionHistory() {

	}

	public TransactionHistory(Account account, Date time, String product, double price, int amount) {
		this.account = account;
		this.product = product;
		this.price = price;
		this.amount = amount;
		this.time = time;
		this.type = "";
        this.closed = 0;
        this.profit = 0;
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

    public int getClosed() {
        return this.closed;
    }

    public void setClosed(int closed) {
        this.closed = closed;
    }

    public double getProfit() {
        return this.profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void close(int closeAmount) {
        this.closed += closeAmount;
    }

    public String toString() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strTime = ft.format(this.time);
        return String.format("<TransactionHistory ID=[%d] AccountID=[%d] Product=[%s]  Type=[%s] Price=[%f] Amount=[%d] Time=[%s]>",
        this.id, this.account.getId(), this.product, this.type,
        this.price, this.amount, strTime);
    }
}
