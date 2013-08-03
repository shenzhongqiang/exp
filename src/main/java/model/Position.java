package model;

public class Position {
	private int id;
	private Account account;
	private String product;
	private int amount;
	private String type;
	
	public Position() {
		
	}
	
	public Position(Account account, String product, int amount) {
		this.account = account;
		this.product = product;
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
}
