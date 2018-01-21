package com.surf.dsasm;

import com.binance.api.client.domain.market.Candlestick;

public class CandleStickUtils {
	public static Double fourPointAverage(Candlestick candle) {
		
		Double open = Double.valueOf(candle.getOpen());
		Double close = Double.valueOf(candle.getClose());
		Double high = Double.valueOf(candle.getHigh());
		Double low = Double.valueOf(candle.getLow() );
		
		return ((open + close + high + low) / 4);
	}
	public static Double fourPointAverageExp(Candlestick candle,int minutes) {
		
		//Divide minutes by 5 to get a more influential weighting
		int timeperiod = minutes / 5;
		Double toReturn = fourPointAverage(candle);
			
		//Apply a weighting formula
		return toReturn +  (2 / (timeperiod + 1) ) * toReturn;
	}
}
