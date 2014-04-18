package main.java.model;

import java.util.*;

public class Account {
	private int id;
	// floating profit loss
	private double floatPl = 0;
	// floating risks
	private double floatRisk = 0;
	// maximum percentage of risks
	private double maxRiskPercent = 0.01;
	// dollar per mini lot (i.e. 1000)
	private double dollarPerPoint = 0.1;
	// account balance
	private double balance;
	// equity = balance + floatPl
	private double equity;
	// used margin, cannot exceed equity
	private double usedMargin = 0;
	private Set<Position> positions = new HashSet<Position>(0);
	public Account() {

	}

	public Account(double balance) {
		this.balance = balance;
		this.equity = balance;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getBalance() {
		return this.balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getEquity() {
		return this.equity;
	}

	public void setEquity(double equity) {
		this.equity = equity;
	}

	public double getUsedMargin() {
		return this.usedMargin;
	}

	public void setUsedMargin(double usedMargin) {
		this.usedMargin = usedMargin;
	}

	public double getFloatPl() {
		return this.floatPl;
	}

	public void setFloatPl(double floatPl) {
		this.floatPl = floatPl;
	}

	public double getFloatRisk() {
		return this.floatRisk;
	}

	public void setFloatRisk(double floatRisk) {
		this.floatRisk = floatRisk;
	}

	public double getMaxRiskPercent() {
		return this.maxRiskPercent;
	}

	public void setMaxRiskPercent(double maxRiskPercent) {
		this.maxRiskPercent = maxRiskPercent;
	}

	public double getDollarPerPoint() {
		return this.dollarPerPoint;
	}

	public void setDollarPerPoint(double dollarPerPoint) {
		this.dollarPerPoint = dollarPerPoint;
	}

	public Set<Position> getPositions() {
		return this.positions;
	}
}
