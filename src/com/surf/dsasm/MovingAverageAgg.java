package com.surf.dsasm;

public class MovingAverageAgg implements Comparable{
	private String symbol;
	private Double agg;
	
	public MovingAverageAgg(String symbol, Double agg) {
		this.symbol = symbol;
		this.agg = agg;
	}
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Double getAgg() {
		return agg;
	}

	public void setAgg(Double agg) {
		this.agg = agg;
	}

	@Override
	public int compareTo(Object o) {
		return agg.compareTo(((MovingAverageAgg) o).getAgg());
	}
}
