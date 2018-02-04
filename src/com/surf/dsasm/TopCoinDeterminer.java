package com.surf.dsasm;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;

/**
 * 
 * Gets all the coins
 * and eth candle sticks for last 12 hours
 *
 */
public class TopCoinDeterminer implements Runnable {

	private static BinanceApiRestClient client;

	private static Timer regulater;

	public static Integer currentCoinIndex = 0;
	public static List<String> allSymbols = new LinkedList<String>();

	public static List<MovingAverageAgg> sortedTopSymbols = new LinkedList<MovingAverageAgg>();
	public static Queue<String> queueGoodCoins = new ConcurrentLinkedQueue<String>();
	public static boolean finished = false;
	
	public TopCoinDeterminer(BinanceApiRestClient client) {
		TopCoinDeterminer.client = client;
	}


	public void run() {
		System.out.println("Generate Queue for the first time");
		generateQueue();
	}
	
	public static void generateQueue() {
		if (client != null) {
			// The reason for the getAll is to look at new coins too
			List<TickerPrice> allPrices = client.getAllPrices();

			// Seperate all the symbols from the current prices
			for (TickerPrice price : allPrices) {
				if (price.getSymbol().endsWith("ETH")) allSymbols.add(price.getSymbol());
			}

			// Then every 20 seconds get the /ETH candlesticks for the last 12 hours
			regulater = new Timer();
			TimerTask task = new CoinCandlestickGetter(client);
			regulater.schedule(task, 0, 2 * 1000);
			
		}
	}
		
	public static void emptyQueue() {
		System.out.println("Queue was empty, regenerating");
		generateQueue();
	}

	public static void stopRetrieval() {
		System.out.println("ListToQueue");
		regulater.cancel();
		listtoQueue();
	}
	private static void listtoQueue() {
		synchronized(sortedTopSymbols) {
			for(MovingAverageAgg agg : sortedTopSymbols) {
				
				synchronized(queueGoodCoins) {
					System.out.println("ADDING "+agg.getSymbol()+" to queue" );
					queueGoodCoins.add(agg.getSymbol());
				}
			}
			System.out.println("Setting finished to true and clearing the list" );
			finished = true;
			sortedTopSymbols.clear();
		}
		
	}
}
