package com.surf.dsasm;

/**
 * essentially a holding area for all variables more for fine tuning the program than anything else - makes it easier to know where to go to change that type of shit
 * @author TwiKitty
 *
 */
public class GlobalVariables {
	
	
	//essentially the number of coins to watch
	public static final int numberOfWatchers = 3;
	
	//The percentage of the highest profit a current coins price is allowed to be before SELL SELL SELL
	public static final Double stopLossCutOff = 0.95;
	
	public static final Double buyingPercentage = 0.005;
	
	public static final Double startingFakeAmount =100d;
	
	public static Double followingEthereumFake = 100d;
}
