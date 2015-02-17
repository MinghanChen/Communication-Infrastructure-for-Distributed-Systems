package messagepasser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Queue;

import multicast.GroupStampedMessage;
import yamlparser.YamlParser;

public  class SrcMonitor implements Runnable {
	Socket socket;
	ObjectInputStream receive;
	Queue<TimeStampedMessage> receivequeue;
	Queue<GroupStampedMessage> deliverqueue;
	Hashtable<String, String> recRuleTable;
	Queue<TimeStampedMessage> recdelay;
	
	
	public SrcMonitor(Queue<TimeStampedMessage> receivequeue, Queue<GroupStampedMessage> deliverqueue, Socket socket, Hashtable<String, String> recRuleTable, Queue<TimeStampedMessage> recdelay) {
		this.socket = socket;
		this.receivequeue = receivequeue;
		this.recRuleTable = recRuleTable;
		this.recdelay = recdelay;
		this.deliverqueue = deliverqueue;
		
		try {
			//din = new DataInputStream(socket.getInputStream());
			receive = new 
		            ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("failed when getting din");
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
					content = (TimeStampedMessage)receive.readObject();
					if(content.getMulti())
						offerMsg(receivequeue, recdelay, content, recRuleTable);
					else{
						synchronized(deliverqueue){
							deliverqueue.offer((GroupStampedMessage)content);
						}
					}
				} catch (ClassNotFoundException e) {
					System.out.println("Cannot transfer to Message type");
					
				} 
				//bw.write(content + "\n");
				//bw.close();
				//System.out.println("The content I receive : " + content);
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
					return;
				}
				while(!recdelay.isEmpty()){
					TimeStampedMessage delayMes = recdelay.poll();
					rcvQueue.offer(delayMes);
					return;
				}			
			
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
			if(recRuleTable.get(key).equals("drop") && !message.getDup())
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
