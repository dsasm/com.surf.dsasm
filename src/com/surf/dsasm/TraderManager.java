package com.surf.dsasm;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;

public class TraderManager {
	public void tryout() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("API-KEY", "SECRET");
		BinanceApiRestClient client = factory.newRestClient();
		TickerStatistics tickerStatistics = client.get24HrPriceStatistics("TRXETH");
		System.out.println(tickerStatistics.getPriceChangePercent());
	}
}
