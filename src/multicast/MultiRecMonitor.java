package multicast;

import java.io.IOException;
import java.net.UnknownHostException;

public class MultiRecMonitor implements Runnable{
	RMulticast multicast;
	public MultiRecMonitor(RMulticast multicast) {
		this.multicast = multicast;
	}
	
	public void run() {
		try {
			multicast.MulticastRec();
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
