package com.surf.dsasm;

import java.util.Timer;
import java.util.TimerTask;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;

public class TraderManager {
	
	public static BinanceApiRestClient client;

	
	public void tryout() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("API-KEY", "SECRET");
		client = factory.newRestClient();
		Timer regulater = new Timer();
		TimerTask task = new TopCoinDeterminer(client);
		regulater.schedule(task , 0, 1000*60*60*12);
	}
}

