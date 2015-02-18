package multicast;

public class OfferCOqueue implements Runnable{
	COMulticast comulticast;
	String groupname;
	
	public OfferCOqueue(COMulticast comulticast, String groupname) {
		this.comulticast = comulticast;
		this.groupname = groupname;
	}
	
	public void run() {
		while (true) {
			try {
				//System.out.println("in the run of OfferCOqueue");
				comulticast.receive(groupname);
			} catch (InterruptedException e) {
				System.err.println("cannot create offerCOqueue()");
			}
		}
		
	}
}
