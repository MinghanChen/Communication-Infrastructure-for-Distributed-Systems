package messagepasser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Stack;

import multicast.GroupStampedMessage;
import yamlparser.YamlParser;

public class Monitor implements Runnable {
	Socket socket;
	ObjectInputStream receive;
	MessagePasser mp;
	Queue<TimeStampedMessage> receivequeue;
	Queue<GroupStampedMessage> deliverqueue;
	//BufferedWriter bw;
	Hashtable<String, String> recRuleTable;

	Hashtable<String, ObjectOutputStream> outputstreamTable;
	Queue<TimeStampedMessage> recdelay;
	Queue<GroupStampedMessage> groupdelay;	
	
	public Monitor(MessagePasser mp, Queue<TimeStampedMessage> receivequeue, Queue<GroupStampedMessage> deliverqueue, Socket socket, Hashtable<String, String> recRuleTable, Queue<TimeStampedMessage> recdelay, Queue<GroupStampedMessage> groupdelay,
			Hashtable<String, ObjectOutputStream> outputstreamTable, ObjectOutputStream sendout) {
		this.socket = socket;
		this.receivequeue = receivequeue;
		this.recRuleTable = recRuleTable;
		this.outputstreamTable = outputstreamTable;
		this.recdelay = recdelay;
		this.groupdelay = groupdelay;
		this.deliverqueue = deliverqueue;
		this.outputstreamTable = outputstreamTable;
		this.mp = mp;
		try {
			receive = new 
		            ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Cannot get inputstream");
		}
	}
	
	public void removeSocket() throws IOException {
		this.socket.close();
	}

	public void run() {

		TimeStampedMessage content;
		try {
			while (true) {
				try {
					content = (TimeStampedMessage) receive.readObject();
					if(!content.getMulti()) {
						offerMsg(receivequeue, recdelay, content, recRuleTable);
					} else{
						if (content.getRequest()) {
							synchronized (mp) {
								mp.handleRequest(content);
							}
						} else if (content.getACK()) {
							//System.out.println("content source :" +content.getSource() + " content.des" + content.getDest());
							mp.addACK((GroupStampedMessage)content);
							//System.out.println("after mp.addACK(content), monitor");
						} else if (content.getRelease()) {
							synchronized (mp) {
								mp.handleRelease();
								//System.out.println("after mp.handleRelease(content), monitor");
							}
						} else {
							deliverMsg(deliverqueue, groupdelay, content, recRuleTable);
						}
					}
				} catch (ClassNotFoundException e) {
					System.out.println("Cannot transfer to Message type");
					
				} 
			}
		} catch (IOException e) {
			System.out.println("Cannot read");
		} finally {
			try {
				this.removeSocket();
			} catch (IOException e) {
				System.out.println("remove failure!");
			}
		}
	}
	
	private void deliverMsg(Queue<GroupStampedMessage> rcvQueue, Queue<GroupStampedMessage> delayQueue, TimeStampedMessage message, Hashtable<String, String> recRuleTable) {
		synchronized (rcvQueue) {
			synchronized (delayQueue) {
				String key =  message.getSource() + message.getDest() + message.getKind() + Integer.toString(message.getNum());
				//System.out.println(key);
				String flag = this.checkReceiveRules(recRuleTable, message, key);
				if(flag.equals("drop"))
					return;
				else if (flag.equals("dupe")){
					rcvQueue.offer((GroupStampedMessage) message);
					TimeStampedMessage newMes = new MessagePasser().clone(message);
					rcvQueue.offer((GroupStampedMessage)newMes);
				}
				else if (flag.equals("delay")){
					recdelay.offer(message);
					return;
				}
				else{
					rcvQueue.offer((GroupStampedMessage)message);
				}
				while(!recdelay.isEmpty()){
					GroupStampedMessage delayMes = groupdelay.poll();
					rcvQueue.offer(delayMes);
				}			
			return;
			}
		}
	}
	
	private void offerMsg(Queue<TimeStampedMessage> rcvQueue, Queue<TimeStampedMessage> delayQueue, TimeStampedMessage message, Hashtable<String, String> recRuleTable) {
		synchronized (rcvQueue) {
			synchronized (delayQueue) {
				
				String key =  message.getSource() + message.getDest() + message.getKind() + Integer.toString(message.getNum());
				
				String flag = this.checkReceiveRules(recRuleTable, message, key);
				
				if(flag.equals("drop"))
					return;
				else if (flag.equals("dupe")){
					rcvQueue.offer(message);
					TimeStampedMessage newMes = new MessagePasser().clone(message);
					rcvQueue.offer(newMes);
				}
				else if (flag.equals("delay")){
					recdelay.offer(message);
					
					return;
				}
				else{
					rcvQueue.offer(message);
					//System.out.println("destination : " + message.getDest());
				}
				while(!recdelay.isEmpty()){
					TimeStampedMessage delayMes = recdelay.poll();
					rcvQueue.offer(delayMes);
				}			
			return;
			}
		}
	}
	
	public String checkReceiveRules(Hashtable<String, String> RuleTable, Message message, String key){
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser("ConfigurationFile.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		recRuleTable = yamlparser.getRecRuleTable();
		
		if(recRuleTable.containsKey(key)){
			if(recRuleTable.get(key).equals("drop") && !message.getDup() )
				return "drop";
			else if(recRuleTable.get(key).equals("duplicate")){
				return "dupe";				
			}
			else if(recRuleTable.get(key).equals("delay")){
				return "delay";
			}
			else
				return "no rule";
		}
		else
			return "no rule";
			
	}
}
