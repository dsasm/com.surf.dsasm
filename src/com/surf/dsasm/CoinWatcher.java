package com.surf.dsasm;

import java.util.List;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;

public class CoinWatcher implements Runnable{
	
	String thisSymbol;
	boolean bought;
	boolean sold;
	boolean running;
	Double highestProfitInPrice;
	Double boughtAt;
	Double soldAt;
	Double quantity;
	
	
	public void run(){
		
		//Get a Symbol to be watching
		synchronized(CoinWatcherManager.toWatch) {
			synchronized (CoinWatcherManager.currentWatchIndex) {
				thisSymbol = CoinWatcherManager.toWatch.get(CoinWatcherManager.currentWatchIndex).getSymbol();
				CoinWatcherManager.currentWatchIndex +=1;
			}
		}
		while (running) {
			while(!bought && running) {
				if ( shouldBuy()) {
					fakeBuy();
				}
				else {
					try {
						Thread.sleep(1000*30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			try {
				watchIntently();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void watchIntently() throws InterruptedException {
		while(bought && !sold) {
			if (shouldSell()) {
				fakeSell();
				sold=true;
			}
			Thread.sleep(1000*15);
		}
	}
	
	public Double toPercentageDiff(Double start, Double close) {
		return ((close -start )/ start)*100;
	}
	
	public boolean shouldBuy() {
		
		synchronized (CoinWatcherManager.client) {
		
			List<Candlestick> candlesticks = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.FIVE_MINUTES);
			Double openingPrice = new Double(candlesticks.get(2).getOpen());
			boolean toReturn = false;
			for(int i = 2; i >=0; i--) {
				Double closingTime = new Double(candlesticks.get(i).getClose());
				Double change = toPercentageDiff(openingPrice, closingTime);
				if (change >= GlobalVariables.buyingPercentage) {
					toReturn = true;
				}
			}
			return toReturn;
		}
	}
	
	public void fakeBuy() {
		synchronized (CoinWatcherManager.client) {
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				quantity = CoinWatcherManager.amountEthereum / lastPrice;
				CoinWatcherManager.amountEthereum -= lastPrice * quantity;
			}
			
		}
	}
	
	public void fakeSell() {
		synchronized (CoinWatcherManager.client) {
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				CoinWatcherManager.amountEthereum += lastPrice * quantity;
			}
			
		}
	}
	public boolean shouldSell() {
		synchronized (CoinWatcherManager.client) {
		List<TickerPrice> bars = CoinWatcherManager.client.getAllPrices();
			for (TickerPrice price : bars) {
				if (price.getSymbol().equals(thisSymbol)) {
					Double priceDiff = new Double(price.getPrice()) - boughtAt;
					Double profitDiff = highestProfitInPrice - boughtAt;
					Double changeToProfit = priceDiff / profitDiff;
					if (changeToProfit < GlobalVariables.stopLossCutOff) {
					    //TODO implement 	getConfidenceInMove();
						return true;
					}
					
				}
			}
		}
		return false;
	}
	
}
