package com.surf.dsasm;
/**
 * Just getters and setters + compare for average
 *
 */
public class MovingAverageAgg implements Comparable<MovingAverageAgg>{
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

	
	public int compareTo(MovingAverageAgg o) {
		// TODO Auto-generated method stub
		return agg.compareTo( o.getAgg());
	}
}
