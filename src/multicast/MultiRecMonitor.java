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
			try {
				multicast.MulticastRec();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException");
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException");
		} catch (IOException e) {
			System.err.println("IOException");
		}
	}
}
