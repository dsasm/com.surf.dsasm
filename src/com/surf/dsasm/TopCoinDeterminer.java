package com.surf.dsasm;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;

/**
 * 
 * Gets all the coins
 * and eth candle sticks for last 12 hours
 *
 */
public class TopCoinDeterminer extends TimerTask {

	private BinanceApiRestClient client;

	private static Timer regulater;

	public static Integer currentCoinIndex = 0;
	public static List<String> allSymbols;

	public static List<MovingAverageAgg> sortedTopSymbols;
	public static boolean finished = false;
	
	public TopCoinDeterminer(BinanceApiRestClient client) {
		this.client = client;
	}

	@Override
	public void run() {

		if (client != null && regulater != null) {
			// The reason for the getAll is to look at new coins too
			List<TickerPrice> allPrices = client.getAllPrices();

			// Seperate all the symbols from the current prices
			for (TickerPrice price : allPrices) {
				allSymbols.add(price.getSymbol());
			}

			// Then every 20 seconds get the /ETH candlesticks for the last 12 hours
			regulater = new Timer();
			TimerTask task = new CoinCandlestickGetter(client);
			regulater.schedule(task, 0, 20 * 1000);
			
			while(currentCoinIndex < allSymbols.size() -1) {
				
			}
			finished = true;
		}
	}

	public static void stopRetrieval() {
		regulater.cancel();
	}

}
