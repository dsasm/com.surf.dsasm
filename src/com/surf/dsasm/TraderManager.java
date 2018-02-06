package com.surf.dsasm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerStatistics;

public class TraderManager {
	
	public static BinanceApiRestClient client;
	public static BufferedWriter writer ;
	
	public void tryout() {
		try {
			writer =  new BufferedWriter(new FileWriter("fakeEthereumTracker.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		List<Candlestick> candles = client.getCandlestickBars("TRXETH", CandlestickInterval.FIVE_MINUTES);
		
		System.out.println("first candle: "+candles.get(0).getHigh());
		System.out.println("last candle: "+candles.get(candles.size()-1).getHigh());
		
		client = factory.newRestClient();
		Timer regulater = new Timer();
		System.out.println();
		System.out.println();
		System.out.println("Starting up Top Coin Determiner");
		TopCoinDeterminer task = new TopCoinDeterminer(client);
		Thread TCD = new Thread(task);
		TCD.start();
		System.out.print("still not finished");
		while (TopCoinDeterminer.queueGoodCoins.size() != GlobalVariables.numberOfWatchers) {
			
			//System.out.print("still not finished");
		}
		System.out.println("TCD FINISHED");
		List<MovingAverageAgg> toWatch = new LinkedList<MovingAverageAgg>(TopCoinDeterminer.sortedTopSymbols);
		System.out.println("Starting up Coin Watcher Manager");
		CoinWatcherManager CWMtask = new CoinWatcherManager(client, toWatch);
		Thread CWM = new Thread(CWMtask);
		CWM.start();
		
	}
	
	public BinanceApiClientFactory createNewFactory(String apiKey, String apiSecret) {
		return BinanceApiClientFactory.newInstance(apiKey, apiSecret);
	}
}

