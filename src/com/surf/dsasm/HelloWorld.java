package com.surf.dsasm;

public class HelloWorld {
	public static void main(String [] args) {
		System.setProperty("apikey", "qqXLspNvSZDeQr3UMQFzifjXjxEVOUhxBRu5wBaxaJzQbjmHyKlSUwJxhBfHv7zb");
		System.setProperty("secret", "erKGeikPr6mUexUKJlyLS1mqdS9Zvno9TP1sRm2Hsn3rKAvJpMslbTVhpIIU1VVk");

		TraderManager tm = new TraderManager();
		tm.tryout();
	}
}
