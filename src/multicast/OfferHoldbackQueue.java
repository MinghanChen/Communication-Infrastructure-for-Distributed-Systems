package multicast;

public class OfferHoldbackQueue implements Runnable{
	COMulticast comulticast;
	String groupname;
	
	public OfferHoldbackQueue(COMulticast comulticast, String groupname) {
		this.comulticast = comulticast;
		this.groupname = groupname;
	}
	
	public void run() {
		while (true) {
			try {
				comulticast.deliver(groupname);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
