package com.surf.dsasm;

import com.binance.api.client.domain.account.Account;

/**
 * just makes it easy to fake methods can implement them in a mock shit here and also put real logic in here
 * @author Dylan
 *
 */
public class BinanceAccountHandler {
	private Account account = null;
	public BinanceAccountHandler(Account currentAccount) {
		this.account = currentAccount;
	}
	
	public Account getAccount() {
		return account;
	}
}
