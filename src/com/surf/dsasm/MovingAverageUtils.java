package com.surf.dsasm;

import java.util.List;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

public class MovingAverageUtils {
	/**
	 * Just does some 4 point average shit but 
	 * @param candlesticks
	 * @param interval
	 * @return
	 */
	public static Double determineMovingAverage(List<Candlestick> candlesticks
			, CandlestickInterval interval) {
		
		Double AmountOfcandlesticks= Double.valueOf(CandlestickIntervalUtils.timeInMinutes(interval)) / 5;
		Double sum = new Double(0);
		int candlestickIndex; 
		//for 1 < number-1 1++
		for(candlestickIndex = 1; candlestickIndex < AmountOfcandlesticks-1; candlestickIndex ++) {
			sum += CandleStickUtils.fourPointAverageExp(candlesticks.get(candlestickIndex), CandlestickIntervalUtils.timeInMinutes(interval) / 5);
		}
		return sum / candlestickIndex;
	}
}
