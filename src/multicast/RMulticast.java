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
	List<Group> groups;
	MessagePasser mp;
	Queue<GroupStampedMessage> deliverqueue;
	HashMap<String, Queue<GroupStampedMessage>> hashmap;
	//ClockService clock = null;

	public RMulticast(MessagePasser mp, List<Group> groups, HashMap<String, Queue<GroupStampedMessage>> hashmap) {
		this.mp = mp;
		this.deliverqueue = mp.getDeliver();
		this.groups = groups;
		
		this.hashmap = hashmap;
		// System.out.println();
		
		new Thread(new MultiRecMonitor(this)).start();

	}

	public void setGroupName(String groupname) {
		this.groupName = groupname;

	}

	public void multicastMsg(GroupStampedMessage groupstampmess, Group thegroup)
			throws NumberFormatException, UnknownHostException, IOException {

		// for(Group group: this.groups){
		// if(group.getGroupName().equals(this.groupName)){
		// int i = group.find(mp.getName());
		// clock = new VectorClock(group.getGroupSize(), i);
		// List<String> members = group.getNames();
		// //tmsg.setMulti();
		// GroupStampedMessage groupstampmess = new
		// GroupStampedMessage(tmsg.getDest(),
		// tmsg.getKind(), tmsg.getData(), tmsg.get_isSendtoLogger(),
		// group.getGroupSize(), this.groupName);
		// clock.increase();
		// groupstampmess.setTimeStamp(clock.getTimeStamp());
		List<String> members = thegroup.getNames();
		if (groupstampmess.getSource().equals(this.mp.getName())) {
			for (String dest : members) {
				groupstampmess.set_dest(dest);
				this.mp.sendMul(groupstampmess);
				if (members.indexOf(dest) == members.size() - 1)
					break;
				this.mp.decrease();
			}
		} else {
			for (String dest : members) {
				groupstampmess.set_dest(dest);
				this.mp.sendMul(groupstampmess);
			}
		}
	}

	public void MulticastRec() throws NumberFormatException,
			UnknownHostException, IOException {
		GroupStampedMessage tmsg = null;

		while (true) {
			synchronized (deliverqueue) {
				tmsg = this.deliverqueue.poll();
			}

			if (tmsg == null) {
				continue;
			}
			Group thegroup = tmsg.get();
			String groupname = thegroup.groupName;
			// GroupStampedMessage timsg;
			for (GroupStampedMessage timsg : hashmap.get(groupname)) {
				if (timsg.getSource().equals(tmsg.getSource())
						&& timsg.getgroupname().equals(tmsg.getgroupname())
						&& groupname.equals(timsg.get().groupName)) {
					return;
				}
			}
			
			synchronized (hashmap.get(groupname)) {
				synchronized (deliverqueue) {
					hashmap.get(groupname).add(tmsg);
					hashmap.get(groupname).notifyAll();
				}
				
			}
			System.out.println(tmsg.getData());
			this.multicastMsg(tmsg, thegroup);
		}

	}
}
