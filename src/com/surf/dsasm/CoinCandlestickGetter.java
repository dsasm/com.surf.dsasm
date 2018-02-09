package com.surf.dsasm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

/**
 * Thread for getting candle stick for a stock
 * 
 *
 */
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
				System.out.println(currentCoin+ " Is the current coin the coincandlestickGetter is on");
			}
			boolean newCoin = false;
			//get the average over the last 12 hours 
			List<Candlestick> candlesticks = client.getCandlestickBars(currentCoin, CandlestickInterval.TWO_HOURLY);
			if (candlesticks.size() < 2)newCoin = true; 
			if (!newCoin) {
				Double largerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.TWO_HOURLY)));
				
				//get the average over the last 4 hours 
				candlesticks = client.getCandlestickBars(currentCoin, CandlestickInterval.FIFTEEN_MINUTES);
				Double smallerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.FIFTEEN_MINUTES)));
				
				Double largerTimeperiodEMA2 = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.TWO_HOURLY)));
				
				//get the average over the last 4 hours 
				candlesticks = client.getCandlestickBars(currentCoin, CandlestickInterval.FIFTEEN_MINUTES);
				Double smallerTimeperiodEMA2 = CandleStickUtils.fourPointAverageExp(candlesticks.get(candlesticks.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.FIFTEEN_MINUTES)));
				
				Double difference = smallerTimeperiodEMA - largerTimeperiodEMA;
				Double percentageDifference = (difference / largerTimeperiodEMA) *100;
				System.out.println("Smaller "+smallerTimeperiodEMA +" | Larger "+ largerTimeperiodEMA);
				MovingAverageAgg newMAAgg = new MovingAverageAgg(currentCoin, percentageDifference);
				Double volume = new Double(client.get24HrPriceStatistics(currentCoin).getVolume());
				System.out.println("Volume : "+volume);
				if (volume >600d && (smallerTimeperiodEMA - largerTimeperiodEMA) > 0 && (smallerTimeperiodEMA2 - largerTimeperiodEMA2) > 0 ) {
					synchronized (TopCoinDeterminer.sortedTopSymbols) {
						
						//Then add to the list of TopSymbols if the list hasnt reached 5 TODO : move 5 into a constant to be managed
						if (TopCoinDeterminer.sortedTopSymbols.size() < GlobalVariables.numberOfWatchers) {
							
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
			if (TopCoinDeterminer.currentCoinIndex == TopCoinDeterminer.allSymbols.size()) {
				
				TopCoinDeterminer.stopRetrieval();
			}
		}	
	}
	
}
