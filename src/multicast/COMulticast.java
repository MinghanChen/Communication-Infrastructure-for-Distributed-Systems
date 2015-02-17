package multicast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import yamlparser.YamlParser;
import messagepasser.MessagePasser;
import messagepasser.TimeStampedMessage;
import clock.ClockService;
import clock.VectorClock;

public class COMulticast {
	private RMulticast rmulticast;
	private HashMap<String, ClockService> clocks = new HashMap<String, ClockService>();
	private ClockService clock;
	private PriorityQueue COqueue;
	private List<Group> groups;
	MessagePasser mp;
	private String groupname;
	private Group thegroup;
	Queue<GroupStampedMessage> deliverqueue;
	
	HashMap<String, ArrayList<GroupStampedMessage>> hashmap = new HashMap<String, ArrayList<GroupStampedMessage>>();
	
	public COMulticast(MessagePasser mp, String configuration_filename) {
		this.mp = mp;
		deliverqueue = mp.getDeliver();
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.groups = yamlparser.getGroups();
		for (int i = 0; i < groups.size(); i++) {
			// System.out.println("The name is " + this.groupName);
			if (groups.get(i).find(mp.getName()) != -1) {
				hashmap.put(groups.get(i).groupName,
						new ArrayList<GroupStampedMessage>());
			}
		}
		
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).find(mp.getName()) != -1) {
				Group group = groups.get(i);
				ClockService cs = new VectorClock(group.getNames().size(), group.find(mp.getName()));
				clocks.put(group.groupName, cs);
			}
		}
		
		rmulticast = new RMulticast(mp, groups, hashmap);
	}
	
	public void setGroup(String groupname) {
		this.groupname = groupname;
	}
	
	public void coMulticast(TimeStampedMessage timessage) {
		
		for (Group group : groups) {
			if (group.groupName.equals(this.groupname)) {
				thegroup = group;
				clock = clocks.get(groupname);
				break;
			}
		}
		if (thegroup == null) {
			return;
		}
		clock.increase();
		GroupStampedMessage gmessage = new GroupStampedMessage(timessage.getDest(),
				timessage.getKind(), timessage.getData(), timessage.get_isSendtoLogger(), thegroup.getGroupSize(), thegroup);
		gmessage.setTimeStamp(clock.getTimeStamp());
		gmessage.set_source(timessage.getSource());
		try {
			rmulticast.multicastMsg(gmessage, thegroup);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
