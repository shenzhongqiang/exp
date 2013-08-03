package model;

import java.util.*;

public class Account {
	private int id;
	private double margin;
	private double floatPl;
	private double floatRisk;
	private double maxRiskPercent = 0.01;
	private double dollarPerPoint = 0.1;
	private Set<Position> positions = new HashSet<Position>(0);
	public Account() {
		
	}
	
	public Account(double margin) {
		this.margin = margin;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getMargin() {
		return this.margin;
	}
	
	public void setMargin(double margin) {
		this.margin = margin;
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
