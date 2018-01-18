package com.surf.dsasm;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;

public class TraderManager {
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

		//ask price and last price are the same given there is something to be bought
		/*
		getCloseTime: 1516317064654
		getOpenTime: 1516230664654
		------
		askPrice: 0.00008190
		bidPrice: 0.00008181
		getLastPrice: 0.00008190
		getLowPrice: 0.00007119
		getOpenPrice: 0.00007144
		------
		priceChangePercentage: 14.642
		getPriceChange: 0.00001046
		 */


	}
	
	public BinanceApiClientFactory createNewFactory(String apiKey, String apiSecret) {
		return BinanceApiClientFactory.newInstance(apiKey, apiSecret);
	}
}
