package multicast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import yamlparser.YamlParser;
import clock.ClockService;
import clock.VectorClock;
import messagepasser.Message;
import messagepasser.MessagePasser;
import messagepasser.TimeStampedMessage;

public class RMulticast {
	String groupName = "";
	List<Group> groups = new ArrayList<Group>();
	MessagePasser mp = new MessagePasser();
	Queue<GroupStampedMessage> deliverqueue;
	HashMap<String, ArrayList<GroupStampedMessage>> hashmap = new HashMap<String, ArrayList<GroupStampedMessage>>();
	ClockService clock = null;
	
	public RMulticast(MessagePasser mp, String configuration_filename){
		this.mp = mp;
		this.deliverqueue = mp.getDeliver();
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.groups = yamlparser.getGroups();
		//System.out.println();
		for (int i = 0; i < groups.size(); i++) {
			//System.out.println("The name is " + this.groupName);
			if (groups.get(i).find(mp.getName()) != -1) {
				hashmap.put(groups.get(i).groupName, new ArrayList<GroupStampedMessage>());
			}
		}
		new Thread(new MultiRecMonitor(this)).start();
		
	}
	
	public void setGroupName(String groupname){
		this.groupName = groupname;

	}
	
	public void MulticastMsg(TimeStampedMessage tmsg) throws NumberFormatException, UnknownHostException, IOException{
		
		for(Group group: this.groups){
			if(group.getGroupName().equals(this.groupName)){
				int i = group.find(mp.getName());
				clock = new VectorClock(group.getGroupSize(), i);
				List<String> members = group.getNames();
				//tmsg.setMulti();
				GroupStampedMessage groupstampmess = new GroupStampedMessage(tmsg.getDest(),
						tmsg.getKind(), tmsg.getData(), tmsg.get_isSendtoLogger(),
						group.getGroupSize(), this.groupName);
				clock.increase();
				groupstampmess.setTimeStamp(clock.getTimeStamp());
				
				if(groupstampmess.getSource().equals(this.mp.getName())){
					for(String dest: members){
						groupstampmess.set_dest(dest);
						this.mp.sendMul(groupstampmess);
						if(members.indexOf(dest)==members.size()-1)
							break;
						this.mp.decrease();
					}
				}
				else{
					for(String dest: members){
						groupstampmess.set_dest(dest);
						this.mp.sendMul(groupstampmess);
					}					
				}
			}
		}
	}
	
	public void MulticastRec() throws NumberFormatException, UnknownHostException, IOException{
		GroupStampedMessage tmsg = null;
		
		while (true) {
			synchronized(deliverqueue) {
				tmsg = this.deliverqueue.poll();
			}
			
			if (tmsg == null) {
				continue;
			}
			String group = tmsg.get();
//			GroupStampedMessage timsg;
			for(GroupStampedMessage timsg : hashmap.get(group)){
				if(timsg.getSource().equals(tmsg.getSource())
						&& timsg.getgroupname().equals(tmsg.getgroupname()) && group.equals(timsg.getgroupname())){
					return;
				}
			}
			hashmap.get(group).add(tmsg);
			System.out.println(tmsg.getData());
			this.MulticastMsg(tmsg);
		}
				
	}
}
