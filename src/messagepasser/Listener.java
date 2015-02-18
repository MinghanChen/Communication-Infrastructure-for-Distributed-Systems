package messagepasser;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Stack;

import multicast.GroupStampedMessage;

public class Listener implements Runnable {
	Hashtable<String, ObjectOutputStream> outputstreamTable;
	Hashtable<String, String> recRuleTable;
	public  Queue<TimeStampedMessage> recdelay;
	ServerSocket ss;
	Socket socket;
	//String n;
	Queue<TimeStampedMessage> receivequeue;
	Queue<GroupStampedMessage> deliverqueue;
	//MessagePasser mp;
	
	HashSet<Monitor> hashset = new HashSet<Monitor>();

	public Listener(Queue<TimeStampedMessage> queue, Queue<GroupStampedMessage> deliverqueue,
					ServerSocket ss, Hashtable<String, ObjectOutputStream> outputstreamTable, Hashtable<String, String> recRuleTable, Queue<TimeStampedMessage> recdelay) { // put hashtable inside
		this.ss = ss;
		//this.socketTable = socketTable;
		this.outputstreamTable = outputstreamTable;
		this.deliverqueue = deliverqueue;
		this.receivequeue = queue;
		System.out.println("A new Listener");
		this.recRuleTable = recRuleTable;
		this.recdelay = recdelay;
		//this.mp = mp;
	}
	
	public void removeSocket() throws IOException {
		socket.close();
	}

	public void run() {
		try {
			while (true) {
				socket = ss.accept();
				ObjectOutputStream sendout = new
			            ObjectOutputStream(socket.getOutputStream());
//				socketTable.put(name, socket);
//				outputstreamTable.put(name, sendout);
				new Thread(new Monitor(receivequeue, deliverqueue, socket, recRuleTable, recdelay, outputstreamTable, sendout)).start();
				//System.out.println("a new Monitor");
			}
		} catch (IOException e) {
			System.out.println("Accept failure");
		} finally {
			try {
				this.removeSocket();
			} catch (IOException e) {
				System.out.println("Remove fails");
			}
		}
	}
}
