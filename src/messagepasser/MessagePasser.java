package messagepasser;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import clock.ClockService;
import clock.LogicalClock;
import clock.VectorClock;
import messageexception.NoIPException;
import messageexception.WrongPortException;
import multicast.GroupStampedMessage;
import multicast.MultiRecMonitor;
import util.BlockQueue;
import yamlparser.YamlParser;

public class MessagePasser {
	int isLogical;
	ClockService clock;
	private Hashtable<String, String> portTableInfo;
	private Hashtable<String, String> sendRuleTable;
	private Hashtable<String, String> recRuleTable;
	public  Queue<TimeStampedMessage> senddelay = new LinkedList<TimeStampedMessage>();
	public  Queue<TimeStampedMessage> recdelay = new LinkedList<TimeStampedMessage>();
	public  Queue<GroupStampedMessage> groupdelay = new LinkedList<GroupStampedMessage>();
	public Queue<TimeStampedMessage> receivequeue;
	public Queue<GroupStampedMessage> deliverqueue;
	public Hashtable<String, ObjectOutputStream> outputstreamTable;
	private String name;
	private int seqNumber;
	private String configuration_filename;
	private String[] loggerinfo;
	
	public MessagePasser(){
		
	}
	public MessagePasser(String configuration_filename,String local_name, boolean[] isFatalError, int isLogical) throws NumberFormatException, IOException {
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		this.configuration_filename = configuration_filename;
		portTableInfo = yamlparser.getPortTableInfo();
		sendRuleTable = yamlparser.getSendRuleTable();
		recRuleTable = yamlparser.getRecRuleTable();
		outputstreamTable = new Hashtable<String, ObjectOutputStream>();
		receivequeue = new LinkedList<TimeStampedMessage>();
		deliverqueue = new LinkedList<GroupStampedMessage>();
		
		this.loggerinfo = yamlparser.getLoggerInfo();
		this.isLogical = isLogical;
		if(!(isLogical == 0))
			clock = new LogicalClock();
		else{
			int position = -1;
			for(int i = 0 ; i < yamlparser.getNames().size(); i++)
				if(local_name.equals(yamlparser.getNames().get(i))){
					position = i;
					break;
				}

			clock = new VectorClock(portTableInfo.size(),position);
		}
		
		this.name = local_name;
		System.out.println("The application belongs to : " + name);
		if (name == null) {
			try {
				throw new NoIPException();
			} catch (NoIPException e) {
				
			} 
			isFatalError[0] = true;
			return;
			//to see if ss == null in the future;
		}
		String port = portTableInfo.get(name).split("\t")[1];
		int portnum = Integer.parseInt(port);
		if (portnum < 1235 || portnum > 65535) {
			System.out.println("Wrong in port number given!");
			try {
				throw new WrongPortException();
			} catch (WrongPortException e) {
				isFatalError[0] = true;
				return;
			}
		}
		ServerSocket ss = new ServerSocket(Integer.parseInt(port));
		new Thread(new Listener( receivequeue, deliverqueue, ss, outputstreamTable, recRuleTable, recdelay, groupdelay)).start();
		
	}
	
	public ClockService getVec(){
		return this.clock;
	}
	
	public int getNum(){
		return this.seqNumber;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Queue<GroupStampedMessage> getDeliver(){
		return this.deliverqueue;
	}
	
	public void increase(){
		this.seqNumber++;
		this.clock.increase();
	}
	
	public void send(Message messageOld) throws NumberFormatException, UnknownHostException, IOException {
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		sendRuleTable = yamlparser.getSendRuleTable();
		
		TimeStampedMessage message = new TimeStampedMessage(messageOld.getDest(),messageOld.getKind(),messageOld.getData(),messageOld.get_isSendtoLogger());
		message.set_source(this.name);
		message.set_seqNum(this.seqNumber);
		clock.increase();
		message.setTimeStamp(clock.getTimeStamp());
		seqNumber++;
		
		String destination = message.getDest();
		String key = name + destination + message.getKind() + message.getNum();
		if (outputstreamTable.containsKey(destination)) {
			ObjectOutputStream sendout = outputstreamTable.get(destination);
			boolean isDelay = this.checkRules(sendRuleTable,sendout, message, key);
			//sendout in checkRules();
			while(!senddelay.isEmpty() && !isDelay){
				TimeStampedMessage delayMes = senddelay.poll();
				sendout.writeObject(delayMes);
			}
		} else {
			String[] info = portTableInfo.get(destination).split("\t");
			Socket socket = new Socket(InetAddress.getByName(info[0]), Integer.parseInt(info[1])); // change for test.
			ObjectOutputStream sendout = new
		            ObjectOutputStream(socket.getOutputStream());
			//System.out.println(key);
			boolean isDelay = this.checkRules(sendRuleTable,sendout, message, key);
			while(!senddelay.isEmpty() && !isDelay){
				TimeStampedMessage delayMes = senddelay.poll();
				sendout.writeObject(delayMes);
			}
			new Thread(new SrcMonitor(receivequeue, deliverqueue, socket, recRuleTable, recdelay,groupdelay)).start();
			System.out.println("a new srcmonitor");
			outputstreamTable.put(destination, sendout);
			
		}
		if (message.get_isSendtoLogger()) {
			this.sendToLogger(message);
		}
		
	}
	
	public void sendToLogger(TimeStampedMessage message) {
		
		try {
			Socket soc = new Socket(InetAddress.getByName(loggerinfo[0]), Integer.parseInt(loggerinfo[1]));
			ObjectOutputStream sendout = new
		            ObjectOutputStream(soc.getOutputStream());
			
			sendout.writeObject(message);
			sendout.close();
			soc.close();
		} catch (IOException e) {
			System.out.println("write to logger fails");
		}
		
		
	}

	public TimeStampedMessage receive() throws IOException {
		
		if (receivequeue.isEmpty()) {
			return null;
		} else {
			TimeStampedMessage msg = receivequeue.poll();
			clock.compare(msg.getTimeStampVec());
			return msg;
		}
	}
	
	public boolean checkRules(Hashtable<String, String> RuleTable, ObjectOutputStream sendout, TimeStampedMessage message, String key) throws IOException{
		if(RuleTable.containsKey(key)){
			if(RuleTable.get(key).equals("drop"))
				return true;
			else if(RuleTable.get(key).equals("duplicate")){
				TimeStampedMessage newMes = this.clone(message);
				message.set_duplicate(true);
				sendout.writeObject(message);
				sendout.writeObject(newMes); // newMes first, message next
			
				return false;
			}
			else if(RuleTable.get(key).equals("delay")){
				this.senddelay.offer(message);
				return true;
			}
			else
				return false;
		}
		else{
			sendout.writeObject(message);
			return false;
		}
		
	}
	
	public String checkReceiveRules(Hashtable<String, String> RuleTable, Message message, String key){
		if(RuleTable.containsKey(key)){
			if(RuleTable.get(key).equals("drop"))
				return "drop";
			else if(RuleTable.get(key).equals("duplicate")){
				return "dupe";				
			}
			else if(RuleTable.get(key).equals("delay")){
				return "delay";
			}
			else
				return "no rule";
		}
		else
			return "no rule";
			
	}
	
	public TimeStampedMessage clone(TimeStampedMessage message){
		TimeStampedMessage mas = new TimeStampedMessage(message.getDest(),message.getKind(),message.getData(),message.get_isSendtoLogger());
		mas.set_duplicate(message.getDup());
		mas.set_source(message.getSource());
		mas.set_seqNum(message.getNum());
		mas.setTimeStamp(message.getTimeStampVec());
		return mas;
	}
	
	public static class RecMessage implements Runnable {
		private MessagePasser messagepasser;
		
		public RecMessage(MessagePasser messagepasser) {
			this.messagepasser = messagepasser;
		}
		public void run () {
			try {
				messagepasser.receive();
			} catch (IOException e) {
				System.out.println("Cannot use in RecMessage");
			}
		}
	}
	
	public void sendMul(GroupStampedMessage message) throws NumberFormatException, UnknownHostException, IOException {
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		sendRuleTable = yamlparser.getSendRuleTable();
		
		message.setMulti();
		message.set_source(this.name);
		
		String destination = message.getDest();
		String key = name + destination + message.getKind() + message.getNum();
		if (outputstreamTable.containsKey(destination)) {
			ObjectOutputStream sendout = outputstreamTable.get(destination);
			boolean isDelay = this.checkRules(sendRuleTable,sendout, message, key);
			//sendout in checkRules();
			while(!senddelay.isEmpty() && !isDelay){
				TimeStampedMessage delayMes = senddelay.poll();
				sendout.writeObject(delayMes);
			}
		} else {
			String[] info = portTableInfo.get(destination).split("\t");
			Socket socket = new Socket(InetAddress.getByName(info[0]), Integer.parseInt(info[1])); // change for test.
			ObjectOutputStream sendout = new
		            ObjectOutputStream(socket.getOutputStream());
			//System.out.println(key);
			boolean isDelay = this.checkRules(sendRuleTable,sendout, message, key);
			while(!senddelay.isEmpty() && !isDelay){
				TimeStampedMessage delayMes = senddelay.poll();
				sendout.writeObject(delayMes);
			}
			new Thread(new SrcMonitor(receivequeue, deliverqueue, socket, recRuleTable, recdelay,groupdelay)).start();
			outputstreamTable.put(destination, sendout);
			
		}
		if (message.get_isSendtoLogger()) {
			this.sendToLogger(message);
		}
		
	}
}



