package com.surf.dsasm;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;

public class TraderManager {
	
	public static BinanceApiRestClient client;

	
	public void tryout() {
		BinanceApiClientFactory factory = createNewFactory(System.getProperty("apikey"),
			System.getProperty("secret"));
		BinanceApiRestClient client = factory.newRestClient();
		TickerStatistics tickerStatistics = client.get24HrPriceStatistics("TRXETH");

		System.out.println("getCloseTime: "+tickerStatistics.getCloseTime());
		System.out.println("getOpenTime: "+tickerStatistics.getOpenTime());
		System.out.println("------");
		System.out.println("askPrice: "+tickerStatistics.getAskPrice());
		System.out.println("bidPrice: "+tickerStatistics.getBidPrice());
		System.out.println("getLastPrice: "+tickerStatistics.getLastPrice());
		System.out.println("getLowPrice: "+tickerStatistics.getLowPrice());
		System.out.println("getOpenPrice: "+tickerStatistics.getOpenPrice());
		System.out.println("------");
		System.out.println("priceChangePercentage: "+tickerStatistics.getPriceChangePercent());
		System.out.println("getPriceChange: "+tickerStatistics.getPriceChange());
		
		client = factory.newRestClient();
		Timer regulater = new Timer();
		System.out.println();
		System.out.println();
		System.out.println("Starting up Top Coin Determiner");
		TopCoinDeterminer task = new TopCoinDeterminer(client);
		Thread TCD = new Thread(task);
		TCD.start();
		
		while (!TopCoinDeterminer.finished) {
			
		}
		System.out.println("TCD FINISHED");
		List<MovingAverageAgg> toWatch = new LinkedList<MovingAverageAgg>(TopCoinDeterminer.sortedTopSymbols);
		System.out.println("Starting up Coin Watcher Manager");
		CoinWatcherManager CWMtask = new CoinWatcherManager(client, toWatch);
		CWMtask.run();
		
	}
	
	public BinanceApiClientFactory createNewFactory(String apiKey, String apiSecret) {
		return BinanceApiClientFactory.newInstance(apiKey, apiSecret);
	}
}

