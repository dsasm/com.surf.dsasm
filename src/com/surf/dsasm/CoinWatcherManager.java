package com.surf.dsasm;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.binance.api.client.BinanceApiRestClient;

public class CoinWatcherManager implements Runnable{
	
	public static BinanceApiRestClient client;
	public static Queue<String> queueOfGoodCoins = new ConcurrentLinkedQueue<String>();
	public static List<MovingAverageAgg> toWatch = new LinkedList<MovingAverageAgg>();
	public static Integer currentWatchIndex;
	private final ExecutorService pool;
	public static Double amountEthereum;
	
	public CoinWatcherManager( BinanceApiRestClient client
	, List<MovingAverageAgg> toWatch) {
		
		//Sets up a Thread pool of Coin Watchers
		amountEthereum  = new Double (100);
		CoinWatcherManager.toWatch = new LinkedList<MovingAverageAgg> (toWatch);
		CoinWatcherManager.client = client;
		System.out.println("Creating CoinWatchers");
		pool = Executors.newFixedThreadPool(GlobalVariables.numberOfWatchers);
	}
	
	
	public void run() {
		
		pool.execute(new CoinWatcher());
		
	}

}
