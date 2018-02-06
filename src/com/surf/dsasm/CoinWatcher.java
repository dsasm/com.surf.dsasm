package com.surf.dsasm;

import java.io.IOException;
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
		boolean skip = false;
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
			long timeStartedWatching = System.currentTimeMillis();

			System.out.println("CoinWatcher - Inside Running");
			//While nothing has been bought
			while(!bought && !skip) {
				boolean shouldBuy = shouldBuy();
				
				List<Candlestick> candlesticksLargerAvg = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.TWO_HOURLY);
				Double largerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticksLargerAvg.get(candlesticksLargerAvg.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.TWO_HOURLY)));
				
				//get the average over the last 4 hours 
				List<Candlestick> candlesticksSmallerEMA = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.FIFTEEN_MINUTES);
				Double smallerTimeperiodEMA = CandleStickUtils.fourPointAverageExp(candlesticksSmallerEMA.get(candlesticksSmallerEMA.size() - 2), new Double(CandlestickIntervalUtils.timeInMinutes(CandlestickInterval.FIFTEEN_MINUTES)));
				
				//Check if a buy should be placed
				System.out.println("CoinWatcher - Should Buy "+shouldBuy);
				if (shouldBuy ) {
					fakeBuy();
				}
				else if (System.currentTimeMillis() - timeStartedWatching > 1000*60*30) {
					System.out.println("This Coin is Taking far too long to show any improvement - skipping and getting new Coin");
					skip = true;
				}
				else if (largerTimeperiodEMA > smallerTimeperiodEMA) {
					System.out.println("This Coin Doesn't Seem viable anymore - regening queue");
					skip = true;
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
			if (bought)System.out.println("CoinWatcher - bought "+thisSymbol);
			//Items have been bought so check on them more often and determine when to sell
			if (!skip) {
				try {
					System.out.println("should watch intently");
					watchIntently();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			getNewCoin();
			bought = false;
			boughtAt = 0d;
			highestProfitInPrice = 0d;
			skip = false;
			sold = false;
		}
		
	}
	
	public void getNewCoin() {
			if (TopCoinDeterminer.queueGoodCoins.size() ==0) {
				System.out.println("CoinWatcher - regen Queue" );
				TopCoinDeterminer.emptyQueue();
			}
			while(TopCoinDeterminer.queueGoodCoins.size() == 0) {
				
			}

			synchronized(TopCoinDeterminer.queueGoodCoins) {
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
			Double openingPrice = new Double(candlesticks.get(candlesticks.size() -3).getHigh());
			Double priceIndicatingASpike = openingPrice * (1+GlobalVariables.buyingPercentage);
			
			List<TickerPrice> prices = CoinWatcherManager.client.getAllPrices();
			for(TickerPrice price : prices) {
				if (price.getSymbol().equals(thisSymbol)) {
					System.out.print("ShouldBuy - opening : "+openingPrice + "  |  neededForBuy : "+priceIndicatingASpike+"  |  current : "+price.getPrice());
					if(Double.valueOf(price.getPrice()) >= priceIndicatingASpike) {
						return gainConfidenceInBuy(new Double(price.getPrice()));
					}
					break;
					
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
				bought = true;
			}
			
		}
	}
	public boolean gainConfidenceInBuy(Double priceBasedOn) {
		//wait a half minute then check if it has increased, then buy
		System.out.println("Gaining Confidence in Buy - passed "+priceBasedOn);
		try {
			Thread.sleep(1000*30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double newPrice = new Double(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
		
		if (newPrice > priceBasedOn) {
			System.out.println("Confidence Gained with new Price "+newPrice);
			return true;
		}
		else if (newPrice == priceBasedOn) return gainConfidenceInBuy(priceBasedOn);
		else{
			System.out.println("Confidence Lost with new Price "+newPrice);
			return false;}
	}
	public boolean gainConfidenceInSell(Double priceBasedOn) {
		//wait a half minute then check if it has increased, then buy
		//wait a half minute then check if it has increased, then buy
		System.out.println("Gaining Confidence in Sell - passed "+priceBasedOn);
		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double newPrice = new Double(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
		if (newPrice < priceBasedOn) {
			System.out.println("Confidence Gained with new Price "+newPrice);
			return true;
		}
		else if (newPrice == priceBasedOn) return gainConfidenceInBuy(priceBasedOn);
		else{
			System.out.println("Confidence Lost with new Price "+newPrice);
			return false;}
	}
	
	public void fakeSell() {
		
		//This just adds the new worth of the number of fake ethereum to the amount of ethereum the program thinks we have
		synchronized (CoinWatcherManager.client) {
			
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				System.out.println("CoinWatcher - selling "+thisSymbol+" | Profit Per: "+(lastPrice - boughtAt )+" | quantity: "+quantity+" | % Incr : "+((lastPrice / boughtAt)*100 )+" | Selling At: "+lastPrice);
				Double lastEthAmount = new Double(CoinWatcherManager.amountEthereum);
				CoinWatcherManager.amountEthereum += lastPrice * quantity;
				try {
					Double newAmount = new Double(CoinWatcherManager.amountEthereum);
					String currAmount = newAmount.toString();
					TraderManager.writer.write( currAmount + " - % incr since last " + ((newAmount / lastEthAmount)*100 )+" - % incr since start "+((newAmount / GlobalVariables.startingFakeAmount)*100) + " - TimeStarted "+TraderManager.timeStarted.toString());
					TraderManager.writer.newLine();
					TraderManager.writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
					Double priceDiff = new Double(price.getPrice());
					
					//highest profit so far is stored as the difference between the price then and the original price
					Double profitDiff = highestProfitInPrice ;
					if (priceDiff > (highestProfitInPrice+boughtAt)) highestProfitInPrice = priceDiff-boughtAt;
					
					
					System.out.println("bought at "+boughtAt+"  |  current price "+priceDiff+"  | highest profit "+(highestProfitInPrice+boughtAt));
					System.out.println("should sell at "+(boughtAt*0.95)+" - OR - "+((highestProfitInPrice*GlobalVariables.stopLossCutOff)+boughtAt));
					
					//if the difference is bigger than the % decreace allowed for a coin then SELL SELL SELL
					if (priceDiff < ((highestProfitInPrice*GlobalVariables.stopLossCutOff)+boughtAt)) {
					    //TODO implement 	getConfidenceInMove();
						return gainConfidenceInSell(priceDiff);
					}
					else if(priceDiff < (boughtAt*0.95)) {
						return true;
					}
					
					//if the new price is bigger than the highestProfitSoFar then replace the highestProfit so far
					
					
				}
			}
		}
		return false;
	}
	
}
