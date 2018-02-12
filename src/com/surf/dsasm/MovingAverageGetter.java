package com.surf.dsasm;

import java.util.List;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

public class MovingAverageGetter {
	
	public static Double getMovingAverageDiff (String thisSymbol, CandlestickInterval largerInt, CandlestickInterval smallerInt) {
		System.out.print("Trying to get "+thisSymbol+" LARGER "+largerInt+" SMALLER "+smallerInt);
		List<Candlestick> candlesticks = CoinWatcherManager.client.getCandlestickBars(thisSymbol, largerInt);
		Double largerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 1), new Double(CandlestickIntervalUtils.timeInMinutes(largerInt)));
		
		candlesticks = CoinWatcherManager.client.getCandlestickBars(thisSymbol, smallerInt);
		Double smallerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 1), new Double(CandlestickIntervalUtils.timeInMinutes(smallerInt)));
		
		return (smallerTimeperiodEMA / largerTimeperiodEMA) *100;
	}
	
	public static Double getMovingAverageDiff (String thisSymbol, CandlestickInterval largerInt, CandlestickInterval smallerInt, BinanceApiRestClient client) {
		System.out.print("Trying to get "+thisSymbol+" LARGER "+largerInt+" SMALLER "+smallerInt);
		List<Candlestick> candlesticks = client.getCandlestickBars(thisSymbol, largerInt);
		Double largerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 1), new Double(CandlestickIntervalUtils.timeInMinutes(largerInt)));
		
		candlesticks = client.getCandlestickBars(thisSymbol, smallerInt);
		Double smallerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 1), new Double(CandlestickIntervalUtils.timeInMinutes(smallerInt)));
		
		return (smallerTimeperiodEMA / largerTimeperiodEMA) *100;
	}
}
