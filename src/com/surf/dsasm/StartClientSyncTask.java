package com.surf.dsasm;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/** basically no logic in here just for starting the user client doesnt actually need to be seperated
 * but makes it way easier to code
 * @author Dylan
 *
 */
public class StartClientSyncTask implements Closeable {

	private static final String CLASS_NAME = StartClientSyncTask.class.getName();
	private static final Logger log = Logger.getLogger(CLASS_NAME);
	
	//would have possible the DB shit initialiser in here if necessary
	
	private UserStartClient client = null; 
	private boolean reconnect = false; 
	private long waitForOpenTime = 0; 
	
	public StartClientSyncTask() {
	}
	
	public void onInit() {
		if((client == null || reconnect) && System.currentTimeMillis() > waitForOpenTime){
			try {
				waitForOpenTime = System.currentTimeMillis() + 60 * 1000 * 5;
				initBinanceCredentials(); 
				start();
				reconnect = true; 
			}catch(Exception e) {
				log.log(Level.SEVERE, "Failed to start UserStartClient");
			}finally {
				reconnect = true; 
			}
			
		}
	}
	
	public void delay(long milliseconds) {
		Integer lock = new Integer(9);
		try {
			synchronized(lock) {
				lock.wait(milliseconds);
			}
		}catch(InterruptedException e) {
			//silence on purpose
		}
	}
	
	private void initBinanceCredentials() {
		System.setProperty("apikey", "qqXLspNvSZDeQr3UMQFzifjXjxEVOUhxBRu5wBaxaJzQbjmHyKlSUwJxhBfHv7zb");
		System.setProperty("secret", "erKGeikPr6mUexUKJlyLS1mqdS9Zvno9TP1sRm2Hsn3rKAvJpMslbTVhpIIU1VVk");
	}
	
	public void reset() {
		this.close();
		waitForOpenTime = 0;
	}
	public void start() throws Exception{
		if(client != null) {
			client.stop();
			client = null;
		}
		
		if(System.getProperty("apikey") != null && System.getProperty("secret") != null) {
			client = new UserStartClient(System.getProperty("apikey"), System.getProperty("secret"));
			client.start();
		}
	} 
	public boolean getReconnectStatus() {
		return client.isConnected();
	}
	
	public void close() {
		if(client != null) {
			client.stop();
			client = null;
		}
		// TODO Auto-generated method stub
		
	}
}
