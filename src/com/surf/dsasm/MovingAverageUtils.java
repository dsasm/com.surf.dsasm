package com.surf.dsasm;

import java.util.List;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

public class MovingAverageUtils {
	public static Double determineMovingAverage(List<Candlestick> candlesticks
			, CandlestickInterval interval) {
		
		int AmountOfcandlesticks= CandlestickIntervalUtils.timeInMinutes(interval) / 5;
		Double sum = new Double(0);
		int candlestickIndex; 
		if (AmountOfcandlesticks - 1 >= candlesticks.size()) AmountOfcandlesticks = candlesticks.size() - 1;
		for(candlestickIndex = 1; candlestickIndex < AmountOfcandlesticks-1; candlestickIndex ++) {
			sum = sum + CandleStickUtils.fourPointAverageExp(candlesticks.get(candlestickIndex), new Double(CandlestickIntervalUtils.timeInMinutes(interval) / 5));
		}
		return sum / candlestickIndex;
	}
}
