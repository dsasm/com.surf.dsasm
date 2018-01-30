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
		running = true;
		//These loops need to be thought through and changed based on how we are going to determine when a thread should stop
		while (running) {
			
			//While nothing has been bought
			while(!bought) {
				
				//Check if a buy should be placed
				if ( shouldBuy()) {
					fakeBuy();
				}
				//Else wait 30 seconds and check again
				else {
					try {
						Thread.sleep(1000*30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			//Items have been bought so check on them more often and determine when to sell
			try {
				watchIntently();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public void watchIntently() throws InterruptedException {
		
		//While the coins havent been sold
		while(!sold) {
			//Check if they should be sold
			if (shouldSell()) {
				fakeSell();
				sold=true;
			}
			//If shouldnt sell, wait 15 seconds and try again
			if (!sold ) Thread.sleep(1000*15);
		}
	}
	
	/**
	 * Determines to percentage change between the first param {@code Double} and the seconds Param
	 * @param start
	 * @param close
	 * @return
	 */
	public Double toPercentageDiff(Double start, Double close) {
		return ((close -start )/ start)*100;
	}
	
	public boolean shouldBuy() {
		
		synchronized (CoinWatcherManager.client) {
			
			//Get the candlesticks in 5 minute chunks
			List<Candlestick> candlesticks = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.FIVE_MINUTES);
			
			//Get the very earliest price which will be used to base whether or not to buy and sell
			Double openingPrice = new Double(candlesticks.get(2).getOpen());
			for(int i = 2; i >=0; i--) {
				//Get each closing price within the last 15 minutes - if the change has increased beyon a certain % then buy else return false
				Double closingTime = new Double(candlesticks.get(i).getClose());
				Double change = toPercentageDiff(openingPrice, closingTime);
				if (change >= GlobalVariables.buyingPercentage) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * For testing viability of an algorithm this method will be used
	 */
	public void fakeBuy() {
		
		
		synchronized (CoinWatcherManager.client) {
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				quantity = CoinWatcherManager.amountEthereum / lastPrice;
				CoinWatcherManager.amountEthereum -= lastPrice * quantity;
				boughtAt = lastPrice;
				highestProfitInPrice = new Double(0);
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
		//Get the latest prices
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
					if (priceDiff > profitDiff) highestProfitInPrice = priceDiff;
					
				}
			}
		}
		return false;
	}
	
}
