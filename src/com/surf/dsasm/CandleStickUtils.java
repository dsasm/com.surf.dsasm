package com.surf.dsasm;

import com.binance.api.client.domain.market.Candlestick;

public class CandleStickUtils {
	/**
	 * Takes the mean of the:
	 * open, close, high & low value
	 * Adds all of them together and divides by 4
	 * @param candle
	 * @return
	 */
	public static Double fourPointAverage(Candlestick candle) {
		
		Double open = Double.valueOf(candle.getOpen());
		Double close = Double.valueOf(candle.getClose());
		Double high = Double.valueOf(candle.getHigh());
		Double low = Double.valueOf(candle.getLow() );
		
		return ((open + close + high + low) / 4);
	}
	
	/**
	 * Divides the minutes by 5, uses @fourPointAverage to get
	 * timePeriod
	 * Takes the 4 point average
	 * Applies weighting formula that does some funky shit
	 * @param candle
	 * @param minutes
	 * @return
	 */
	public static Double fourPointAverageExp(Candlestick candle, Double minutes) {
		
		//Divide minutes by 5 to get a more influential weighting
		Double timeperiod = minutes / 5;
		Double toReturn = fourPointAverage(candle);
			
		//Apply a weighting formula
		//Only put this variable in for debugging so can have a look at shit 
		//aids to debug immediate returns 
		Double retVal = toReturn +  (2 / (timeperiod + 1) ) * toReturn;
		return retVal;
	}
}
