package com.surf.dsasm;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.OrderRequest;

public class UserStartClient implements Runnable {

	private static final String CLASS_NAME = UserStartClient.class.getName();
	private static final Logger log = Logger.getLogger(CLASS_NAME);

	// Binance Client to get current stocks
	private boolean connected = false;
	private boolean stopped = false;
	private String connectionId = null;
	private Thread threadToActuallyDoShit;
	private BinanceApiRestClient binanceClient = null;
	private BinanceApiClientFactory binanceFactory = null;
	private BinanceAccountHandler accountHandler = null;
	private long timeSinceRestart = 0;

	public UserStartClient(String apiKey, String apiSecret) {
		this.binanceFactory = BinanceApiClientFactory.newInstance(apiKey, apiSecret);
		this.binanceClient = binanceFactory.newRestClient();
		this.accountHandler = new BinanceAccountHandler(binanceClient.getAccount());

	}

	public void run() {
		while(!stopped) {
			try {
				boolean firstRun = true; 
				boolean noTrades = false;
				//anything needed to be run on first start up
				//this will always be run fist and then initialise other shit depending on outcome
				
				noNeed: if(firstRun) {
					timeSinceRestart = System.currentTimeMillis();
					//First thing to do is get active orders, need order Ids and then need to start threads
					//which are to monitor these 
					OrderRequest activeRequest = new OrderRequest("active");
					OrderStatus status1 = OrderStatus.PARTIALLY_FILLED;
					OrderStatus status2 = OrderStatus.NEW;

					ArrayList<Order> allOrders = (ArrayList<Order>) binanceClient.getOpenOrders(activeRequest);
					ArrayList<Order> activeOrders = new ArrayList<Order>(); 
					for(Order currOrder : allOrders) {
						if(status1.equals(currOrder.getStatus()) || status2.equals(currOrder.getStatus())){
							activeOrders.add(currOrder);
						}
					}
					
					if(activeOrders.isEmpty()) {
						noTrades = true; 
						break noNeed;
					}
					
					for(Order activeOrder : activeOrders) {
						handleOldActiveOrders(activeOrder.getOrderId(), activeOrder.getExecutedQty(),
								activeOrder.getStopPrice(), activeOrder.getType(), activeOrder.getTime());
					}
				}else {
					while(timeSinceRestart + 5 * 60 * 1000 > System.currentTimeMillis()) {
						delay(30000);
					}
				}
				// at this point its dealt with open orders and we can do whatever the fuck is needed
				connected = true; 
			}catch(Exception e) {
					if(log.isLoggable(Level.ALL)) {
						log.log(Level.WARNING, "something bad happened and everthing broke");
					}
				}finally {
					if(!stopped) {
						log.log(Level.ALL, "making watch thread wait before trying again");
						delay(30000);
					}
				}
			
		}
	}
	
	private void handleOldActiveOrders(Long long1, String id, String exectutedAmount, OrderType stopPrice, long timeOfOrder) {
	//TODO 	
	}
	private void delay(long waitInMillis) {
		try {
			synchronized (this) {
				this.wait(waitInMillis);
			}
		} catch (InterruptedException e) {
			// Left empty on purpose
		}
	}
}
