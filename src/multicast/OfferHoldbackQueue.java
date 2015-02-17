package multicast;

public class OfferHoldbackQueue implements Runnable{
	COMulticast comulticast;
	String groupname;
	
	public OfferHoldbackQueue(COMulticast comulticast, String groupname) {
		this.comulticast = comulticast;
		this.groupname = groupname;
	}
	
	public void run() {
		comulticast.deliver(groupname);
	}
}
