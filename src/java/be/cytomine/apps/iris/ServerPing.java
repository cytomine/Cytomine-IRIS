package be.cytomine.apps.iris;

import java.io.File;

public class ServerPing extends PropertyManager {

	private static ServerPing instance;
	
	public static ServerPing getInstance() {
		if (ServerPing.instance == null){
			ServerPing.instance = new ServerPing();
		}
		return ServerPing.instance;
	}
	
	static String tmpRoot = System.getProperty("java.io.tmpdir");
	static String fileName = tmpRoot + File.separator + "ServerPing.properties";
	String failKey = "pingfailcount";
	
	public ServerPing() {
		super(new File(fileName));
		// write the default mapping file, if there is no mapping available
		if (!this.propFile.exists()) {
			// write defaults
			this.resetFailCount();
		} else {
			// initialize the property manager with the new file
			this.read();
		}
	}

	/**
	 * Increments the fail count by one and returns the new number.
	 * @return
	 */
	public int incrementFailCount(){
		int oldCount = getFailCount();
		int newCount = oldCount+1;
		this.setProperty(failKey, String.valueOf(newCount));
		return newCount;
	}
	
	/**
	 * Gets the fail count.
	 */
	public int getFailCount(){
		return Integer.valueOf(this.getProperty(failKey)).intValue();
	}
	
	/**
	 * Resets the fail count to zero.
	 */
	public void resetFailCount(){
		this.setProperty(failKey, "0");
	}
}
