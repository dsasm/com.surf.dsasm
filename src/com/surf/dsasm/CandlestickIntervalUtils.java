package com.surf.dsasm;

import com.binance.api.client.domain.market.CandlestickInterval;

public class CandlestickIntervalUtils {
	
	/**
	 * Takes a time, switches on it and returns the amount of minutes that interval is.
	 * E.g. 12 Hours is 720 minutes
	 * @param time
	 * @return
	 */
	public static int timeInMinutes(CandlestickInterval time) {
		switch (time) {
			case ONE_MINUTE:
				return 1;
				
			case THREE_MINUTES:
				return 3;
				
			case FIVE_MINUTES:
				return 5;
				
			case FIFTEEN_MINUTES:
				return 15;
			
			case HALF_HOURLY:
				return 30;
			
			case HOURLY:
				return 60;
			
			case TWO_HOURLY:
				return 120;
				
			case SIX_HOURLY:
				return 360;
				
			case EIGHT_HOURLY:
				return 480;
				
			case TWELVE_HOURLY:
				return 720;
				
			case DAILY:
				return 1440;
				
			case THREE_DAILY:
				return 4320;
				
			case WEEKLY:
				return 10080;
			default:
				return 240;
		}
	}

}
