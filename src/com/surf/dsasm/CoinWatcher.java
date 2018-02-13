package com.surf.dsasm;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
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
	int isSelling = 0;
	boolean hasPassedAThreshold = false;
	public void run(){
		boolean skip = false;
		//Get a Symbol to be watching
		synchronized (TopCoinDeterminer.queueGoodCoins) {
			while(TopCoinDeterminer.queueGoodCoins.size() == 0) {
				System.out.println(TopCoinDeterminer.queueGoodCoins.size());
			}
			thisSymbol =TopCoinDeterminer.queueGoodCoins.poll();
			System.out.println("Thread Accepted "+thisSymbol);
		}
		running = true;
		//These loops need to be thought through and changed based on how we are going to determine when a thread should stop
		while (running) {
			hasPassedAThreshold = false;
			long timeStartedWatching = System.currentTimeMillis();

			System.out.println("CoinWatcher - "+thisSymbol+" - Inside Running");
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
			if (bought)System.out.println("CoinWatcher - "+thisSymbol+" - bought "+thisSymbol);
			//Items have been bought so check on them more often and determine when to sell
			if (!skip) {
				try {
					System.out.println("CoinWatcher - "+thisSymbol+" - should watch intently");
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
		synchronized(TopCoinDeterminer.queueGoodCoins) {
			if (TopCoinDeterminer.queueGoodCoins.size() ==0) {
				System.out.println("CoinWatcher - "+thisSymbol+" - regen Queue" );
				thisSymbol = null;
				TopCoinDeterminer.emptyQueue();
			}
			while(TopCoinDeterminer.retrieving || TopCoinDeterminer.queueGoodCoins.size() == 0) {
				
			}
			
			thisSymbol = TopCoinDeterminer.queueGoodCoins.poll();

			System.out.println("CoinWatcher - "+thisSymbol+" - get new coin");
		}
	}
	public void watchIntently() throws InterruptedException {
		
		//While the coins havent been sold
		while(!sold) {
			//Check if they should be sold
			System.out.println("About to check if it should sell");
			if (shouldSell()) {
				fakeSell();
				sold=true;
			}
			//If shouldnt sell, wait 15 seconds and try again
			if (!sold ) Thread.sleep(1000*5);
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
//			System.out.println("CoinWatcher - "+thisSymbol+" - shouldBuy?");
//			//Get the candlesticks in 5 minute chunks
//			List<Candlestick> candlesticks = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.FIVE_MINUTES);
//			
//			
//			
//			//Get the very earliest price which will be used to base whether or not to buy and sell
//			Double openingPrice = new Double(candlesticks.get(candlesticks.size() -3).getHigh());
//			Double priceIndicatingASpike = openingPrice * (1+GlobalVariables.buyingPercentage);
//			
//			List<TickerPrice> prices = CoinWatcherManager.client.getAllPrices();
//			for(TickerPrice price : prices) {
//				if (price.getSymbol().equals(thisSymbol)) {
//					System.out.print("CoinWatcher - "+thisSymbol+" - ShouldBuy - opening : "+openingPrice + "  |  neededForBuy : "+priceIndicatingASpike+"  |  current : "+price.getPrice());
//					if(Double.valueOf(price.getPrice()) >= priceIndicatingASpike) {
//						return gainConfidenceInBuy(new Double(price.getPrice()));
//					}
//					break;
//					
//				}
//			}
			Double MADiff = MovingAverageGetter.getMovingAverageDiff(thisSymbol, CandlestickInterval.FIFTEEN_MINUTES, CandlestickInterval.FIVE_MINUTES);
			List<Candlestick> candlesticks2 = CoinWatcherManager.client.getCandlestickBars(thisSymbol, CandlestickInterval.FIVE_MINUTES);
			Double latestOpen = Double.valueOf(candlesticks2.get(candlesticks2.size() - 1).getOpen());
			Double latestClose = Double.valueOf(candlesticks2.get(candlesticks2.size() - 1).getClose());
			System.out.println("SYMBOL: "+thisSymbol+" should buy? %diff MA = "+MADiff.toString() +" latestOpen  : "+latestOpen+" vs latestClose : "+latestClose);
			if (MADiff > 1.001 && latestClose > latestOpen) return gainConfidenceInBuy(latestClose);
			
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
		List<Boolean> confArray =  new ArrayList<Boolean>();
		int counter = 0;
		boolean stillGettingConf = true;
		while(counter < 3 ) {
			System.out.println("CoinWatcher - "+thisSymbol+" - Gaining Confidence in Buy - passed "+priceBasedOn);
			try {
				Thread.sleep(1000*5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Double newPrice = new Double(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			Double percDiff = newPrice / priceBasedOn * 100;
			if (percDiff > 100.05) {
				System.out.println("CoinWatcher - "+thisSymbol+" - Confidence Gained with new Price "+newPrice);
				counter ++;
				confArray.add(true);
				priceBasedOn = newPrice;
			}
			else if (percDiff < 99.95) {
				System.out.println("CoinWatcher - "+thisSymbol+" - Confidence Lost with new Price "+newPrice);
				counter++;
				confArray.add(false);
				priceBasedOn = newPrice;
			}
		}
		int confCounter = 0;
		System.out.print("Confidence OutCome: ");
		for (int i = 0; i < confArray.size(); i ++) {
			System.out.print(confArray.get(i)+" - ");
			if (confArray.get(i)) confCounter++;
		}
		System.out.print(" returning : "+confCounter);
		return (confCounter >= 2);
	}
	public boolean gainConfidenceInSell(Double priceBasedOn) {
		//wait a half minute then check if it has increased, then buy
		//wait a half minute then check if it has increased, then buy
		System.out.println("CoinWatcher - "+thisSymbol+" - Gaining Confidence in Sell - passed "+priceBasedOn);
		isSelling = isSelling +1;
		try {
			Thread.sleep(1000*10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double newPrice = new Double(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
		if (newPrice < priceBasedOn) {
			System.out.println("CoinWatcher - "+thisSymbol+" - Confidence Gained with new Price "+newPrice);
			return true;
		}
		else if (newPrice == priceBasedOn) return gainConfidenceInBuy(priceBasedOn);
		else if(isSelling >3) {
			isSelling=0;
			System.out.println("CoinWatcher - "+thisSymbol+" - Confidence Gained with how long its been hodling");
			return true;
		}
		else{
			System.out.println("CoinWatcher - "+thisSymbol+" - Confidence Lost with new Price "+newPrice);
			return false;}
	}
	
	public void fakeSell() {
		
		//This just adds the new worth of the number of fake ethereum to the amount of ethereum the program thinks we have
		synchronized (CoinWatcherManager.client) {
			
			Double lastPrice = Double.valueOf(CoinWatcherManager.client.get24HrPriceStatistics(thisSymbol).getLastPrice());
			synchronized (CoinWatcherManager.amountEthereum) {
				System.out.println("CoinWatcher - "+thisSymbol+" - 1 - CoinWatcher - selling "+thisSymbol+" | Profit Per: "+(lastPrice - boughtAt )+" | quantity: "+quantity+" | % Incr : "+((lastPrice / boughtAt)*100 )+" | Selling At: "+lastPrice);
				Double lastEthAmount = new Double(CoinWatcherManager.amountEthereum);
				CoinWatcherManager.amountEthereum += lastPrice * quantity;
				try {
					Double newAmount = new Double(CoinWatcherManager.amountEthereum);
					String currAmount = newAmount.toString();
					TraderManager.writer.write( "2 - "+currAmount + " - % incr since last " + ((newAmount / lastEthAmount)*100 )+" - % incr since start "+((newAmount / GlobalVariables.startingFakeAmount)*100) + " - TimeStarted "+TraderManager.timeStarted.toString()+" - Trade made at"+LocalTime.now().toString()+"\n");
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
					if ((highestProfitInPrice+boughtAt) / boughtAt > 1.002) hasPassedAThreshold = true; 
					//highest profit so far is stored as the difference between the price then and the original price
					Double profitDiff = highestProfitInPrice ;
					if (priceDiff > (highestProfitInPrice+boughtAt)) highestProfitInPrice = priceDiff-boughtAt;
					
					
					System.out.println("CoinWatcher - "+thisSymbol+" - bought at "+boughtAt+"  |  current price "+priceDiff+"  | highest profit "+(highestProfitInPrice+boughtAt));
					System.out.println("CoinWatcher - "+thisSymbol+" - should sell at "+(boughtAt*0.95)+" - OR - "+((highestProfitInPrice*GlobalVariables.stopLossCutOff)+boughtAt));
					
					//if the difference is bigger than the % decreace allowed for a coin then SELL SELL SELL
					if(hasPassedAThreshold && ((highestProfitInPrice+boughtAt) / boughtAt < 1.002)) return true;

					else if ((priceDiff-boughtAt) /(highestProfitInPrice) < 0.97) return true;
					else if(priceDiff < (boughtAt*0.98)) {
						return true;
					}
					
					else if (priceDiff < ((highestProfitInPrice*GlobalVariables.stopLossCutOff)+boughtAt )
							&& ((priceDiff-boughtAt) /(highestProfitInPrice) > 0.97) ) {
					    //TODO implement 	getConfidenceInMove();
						return gainConfidenceInSell(priceDiff);
					}
					//if the new price is bigger than the highestProfitSoFar then replace the highestProfit so far
					
					
				}
			}
		}
//		Double MADiff = MovingAverageGetter.getMovingAverageDiff(thisSymbol, CandlestickInterval.FIFTEEN_MINUTES, CandlestickInterval.FIVE_MINUTES);
//		System.out.print("SYMBOL: "+thisSymbol+" should sell? %diff MA = "+MADiff.toString());
//		if (MADiff < 1.001) return true;
		return false;
	}
	
}
