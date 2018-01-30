package com.surf.dsasm;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.binance.api.client.BinanceApiRestClient;

public class CoinWatcherManager extends TimerTask{
	
	public static BinanceApiRestClient client;
	
	public static List<MovingAverageAgg> toWatch = new LinkedList<MovingAverageAgg>();
	public static Integer currentWatchIndex;
	private final ExecutorService pool;
	public static Double amountEthereum;
	
	public CoinWatcherManager( BinanceApiRestClient client
	, List<MovingAverageAgg> toWatch) {
		amountEthereum  = new Double (100);
		CoinWatcherManager.toWatch = new LinkedList<MovingAverageAgg> (toWatch);
		CoinWatcherManager.client = client;
		pool = Executors.newFixedThreadPool(GlobalVariables.numberOfWatchers);
	}
	
	
	@Override
	public void run() {
		
		pool.execute(new CoinWatcher());
		
	}

}
