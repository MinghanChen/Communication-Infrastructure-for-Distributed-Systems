package multicast;

public class OfferCOqueue implements Runnable{
	COMulticast comulticast;
	String groupname;
	
	public OfferCOqueue(COMulticast comulticast, String groupname) {
		this.comulticast = comulticast;
		this.groupname = groupname;
	}
	
	public void run() {
		try {
			comulticast.receive(groupname);
		} catch (InterruptedException e) {
			System.err.println("cannot create offerCOqueue()");
		}
	}
}
