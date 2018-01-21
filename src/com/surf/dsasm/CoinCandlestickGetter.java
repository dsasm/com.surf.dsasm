package com.surf.dsasm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

public class CoinCandlestickGetter extends TimerTask{
	
	BinanceApiRestClient client;
	List<Double> averages;
	
	public CoinCandlestickGetter(BinanceApiRestClient client) {
		this.client =client;
	}
	
	@Override
	public void run() {
		
		synchronized (TopCoinDeterminer.allSymbols) {
			
			//get a new Averages arrayList
			averages = new LinkedList<Double>();
			String currentCoin = null;
			
			//Get the current coin + symbol
			synchronized (TopCoinDeterminer.currentCoinIndex ) {
				currentCoin = TopCoinDeterminer.allSymbols.get(TopCoinDeterminer.currentCoinIndex);
				TopCoinDeterminer.currentCoinIndex =TopCoinDeterminer.currentCoinIndex +1; 
			}
			
			//get the average over the last 12 hours 
			List<Candlestick> candlesticks = client.getCandlestickBars(currentCoin, CandlestickInterval.TWELVE_HOURLY);
			averages.add(CandleStickUtils.fourPointAverageExp(candlesticks.get(0), 12*60));
			
			//get the average over the last 4 hours 
			candlesticks = client.getCandlestickBars(currentCoin, CandlestickInterval.FOUR_HORLY);
			averages.add(CandleStickUtils.fourPointAverageExp(candlesticks.get(0), 4*60));
			
			MovingAverageAgg newMAAgg = new MovingAverageAgg(currentCoin, averages.get(0) - averages.get(1));
			
			synchronized (TopCoinDeterminer.sortedTopSymbols) {
				
				//Then add to the list of TopSymbols if the list hasnt reached 5 TODO : move 5 into a constant to be managed
				if (TopCoinDeterminer.sortedTopSymbols.size() < 5) {
					
					TopCoinDeterminer.sortedTopSymbols.add(newMAAgg);
					
					//Then sort
					Collections.sort(TopCoinDeterminer.sortedTopSymbols, Collections.reverseOrder());
				}
				
				//else if the lowest agg within the list is lower than this agg, remove it, add this new one and sort
				else if(TopCoinDeterminer.sortedTopSymbols.get(TopCoinDeterminer.sortedTopSymbols.size() - 1).getAgg()
							< newMAAgg.getAgg()) {
					
					TopCoinDeterminer.sortedTopSymbols.remove(TopCoinDeterminer.sortedTopSymbols.size() - 1);
					TopCoinDeterminer.sortedTopSymbols.add(newMAAgg);
					Collections.sort(TopCoinDeterminer.sortedTopSymbols, Collections.reverseOrder());
				}
			}
		}
		
	}
	
}