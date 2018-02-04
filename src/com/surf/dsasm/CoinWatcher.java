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
		synchronized (TopCoinDeterminer.queueGoodCoins) {
				while(TopCoinDeterminer.queueGoodCoins.size() == 0) {
					System.out.println(TopCoinDeterminer.queueGoodCoins.size());
				}
				thisSymbol =TopCoinDeterminer.queueGoodCoins.poll();
		}
		running = true;
		//These loops need to be thought through and changed based on how we are going to determine when a thread should stop
		while (running) {

			System.out.println("CoinWatcher - Inside Running");
			//While nothing has been bought
			while(!bought) {
				boolean shouldBuy = shouldBuy();
				//Check if a buy should be placed
				System.out.println("CoinWatcher - Should Buy "+shouldBuy);
				if (shouldBuy ) {
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
			System.out.println("CoinWatcher - bought "+thisSymbol);
			//Items have been bought so check on them more often and determine when to sell
			try {
				watchIntently();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getNewCoin();
		}
		
	}
	
	public void getNewCoin() {
		synchronized(TopCoinDeterminer.queueGoodCoins) {
			if (TopCoinDeterminer.queueGoodCoins.size() ==0) {
				System.out.println("CoinWatcher - regen Queue" );
				TopCoinDeterminer.emptyQueue();
			}
			while(TopCoinDeterminer.queueGoodCoins.size() == 0) {
				
			}
			System.out.println("CoinWatcher - get new coin");
			thisSymbol = TopCoinDeterminer.queueGoodCoins.poll();
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
			Double openingPrice = new Double(candlesticks.get(candlesticks.size() -3).getOpen());
			for(int i = 3; i >0; i--) {
				//Get each closing price within the last 15 minutes - if the change has increased beyon a certain % then buy else return false
				Double closingTime = new Double(candlesticks.get(candlesticks.size() -i).getClose());
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
		//This is alot of code just to say change the amount of fake ethereum the program thinks it has and to reset the highestProfit so far
		
		synchronized (CoinWatcherManager.client) {
			
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				//Get the amount of coins that can be bought
				quantity = CoinWatcherManager.amountEthereum / lastPrice;
				
				//subtract that amount * price from the amount of Ethereum that is faked 
				CoinWatcherManager.amountEthereum -= lastPrice * quantity;
				
				//record the price it was bought at
				boughtAt = lastPrice;
				
				//Ensure the highestProfit is 0 as this is a buy 
				highestProfitInPrice = new Double(0);
			}
			
		}
	}
	
	public void fakeSell() {
		
		//This just adds the new worth of the number of fake ethereum to the amount of ethereum the program thinks we have
		synchronized (CoinWatcherManager.client) {
			
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				System.out.println("CoinWatcher - selling "+thisSymbol+" | Profit: "+(boughtAt - lastPrice)*quantity );
				CoinWatcherManager.amountEthereum += lastPrice * quantity;
				quantity = 0d;
			}
		}
	}
	public boolean shouldSell() {
		synchronized (CoinWatcherManager.client) {
			//Get the latest prices
			List<TickerPrice> prices = CoinWatcherManager.client.getAllPrices();
			//for each price find the one that is m,anaged by this thread
			for (TickerPrice price : prices) {
				if (price.getSymbol().equals(thisSymbol)) {
					
					//work out the difference between the original price and the price now
					Double priceDiff = new Double(price.getPrice()) - boughtAt;
					
					//highest profit so far is stored as the difference between the price then and the original price
					Double profitDiff = highestProfitInPrice;
					
					//work out the percentage change since the highest profit and now (and protect against dividing by 0)
					Double changeToProfit = (profitDiff == 0) ? 0 : priceDiff / profitDiff;
					
					//if the new price is bigger than the highestProfitSoFar then replace the highestProfit so far
					if (priceDiff > profitDiff) highestProfitInPrice = priceDiff;
					
					//if the difference is bigger than the % decreace allowed for a coin then SELL SELL SELL
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
