package multicast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
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
	private HashMap<String, ClockService> clocks;
	// private ClockService clock;
	private Hashtable<String, PriorityQueue<GroupStampedMessage>> COqueuestable;
	private Hashtable<String, Queue<GroupStampedMessage>> holdback_queue;
	private boolean isAvailable = false;
	private List<Group> groups;
	private MessagePasser mp;
	private String groupname;
	private Group thegroup;
	Queue<GroupStampedMessage> deliverqueue;

	List<String> inclu_groups;

	HashMap<String, Queue<GroupStampedMessage>> hashmap = new HashMap<String, Queue<GroupStampedMessage>>();

	public COMulticast(MessagePasser mp, String configuration_filename) {
		this.mp = mp;
		inclu_groups = new ArrayList<String>();
		deliverqueue = mp.getDeliver();
		clocks = new HashMap<String, ClockService>();
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.groups = yamlparser.getGroups();
		COqueuestable = new Hashtable<String, PriorityQueue<GroupStampedMessage>>();
		Comparator<GroupStampedMessage> comparator = new CompareGroupMess();

		for (int i = 0; i < groups.size(); i++) {
			// System.out.println("The name is " + this.groupName);
			if (groups.get(i).find(mp.getName()) != -1) {
				String name = groups.get(i).groupName;
				hashmap.put(name, new LinkedList<GroupStampedMessage>());
				inclu_groups.add(name);
				COqueuestable.put(name, new PriorityQueue<GroupStampedMessage>(
						20, comparator));
				holdback_queue.put(name, new LinkedList<GroupStampedMessage>());
				new Thread(new OfferCOqueue(this, name));
				new Thread(new OfferHoldbackQueue(this, name));

			}
		}

		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).find(mp.getName()) != -1) {
				Group group = groups.get(i);
				clocks.put(group.groupName, new VectorClock(group.getNames()
						.size(), group.find(mp.getName())));
			}
		}

		rmulticast = new RMulticast(mp, groups, hashmap);
	}

	public void setGroup(String groupname) {
		this.groupname = groupname;
	}

	public void coMulticast(TimeStampedMessage timessage) {
		synchronized (clocks.get(groupname)) {
			ClockService clock = null;
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
			GroupStampedMessage gmessage = new GroupStampedMessage(
					timessage.getDest(), timessage.getKind(),
					timessage.getData(), timessage.get_isSendtoLogger(),
					thegroup.getGroupSize(), thegroup);
			gmessage.setTimeStamp(clock.getTimeStamp());
			gmessage.set_source(timessage.getSource());
			try {
				rmulticast.multicastMsg(gmessage, thegroup);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void receive(String groupname) throws InterruptedException {
		PriorityQueue<GroupStampedMessage> COqueue = COqueuestable
				.get(groupname);
		Queue<GroupStampedMessage> delayqueue = hashmap.get(groupname);
		synchronized (delayqueue) {
			synchronized (COqueue) {
				while (delayqueue.isEmpty()) {
					delayqueue.wait();
				}
				COqueue.offer(delayqueue.poll());
				isAvailable = true;
				COqueue.notifyAll();
			}
		}
	}

	public void deliver(String groupname) {
		PriorityQueue<GroupStampedMessage> COqueue = COqueuestable
				.get(groupname);
		Queue<GroupStampedMessage> queue = holdback_queue.get(groupname);
		synchronized (COqueue) {
			synchronized (queue) {
				while (COqueue.isEmpty() || !isAvailable) {
					try {
						COqueue.wait();
					} catch (InterruptedException e) {
						System.err.println("error when wait");
					}
				}

				if (COsort(groupname, COqueue.peek())) {
					queue.offer(COqueue.poll());
					synchronized (clocks.get(groupname)) {
						ClockService cs = clocks.get(groupname);
						cs.compare(queue.peek().getTimeStamp());
					}

				}
				isAvailable = false;
			}
		}
	}

	public void getGMessage(String groupname) {
		Queue<GroupStampedMessage> queue = holdback_queue.get(groupname);
		synchronized (queue) {
			if (!queue.isEmpty()) {
				GroupStampedMessage GMessage = queue.poll();
				GMessage.print();
			}
		}
	}

	public boolean COsort(String groupname, GroupStampedMessage g1) {
		boolean isNext = false;
		ClockService cs = clocks.get(groupname);
		int[] g1vec = g1.getTimeStamp();
		int pos = cs.getPosition();
		int counter = 0;
		for (int i = 0; i < g1vec.length; i++) {
			if (cs.getTimeStamp()[i] >= g1vec[i]) {
				counter++;
			}
		}

		if (cs.getTimeStamp()[pos] != (g1vec[pos] + 1)
				&& counter == (g1vec.length - 1)) {
			isNext = true;
		}

		return isNext;

	}

}

class CompareGroupMess implements Comparator<GroupStampedMessage> {
	// @Override
	public int compare(GroupStampedMessage g1, GroupStampedMessage g2) {
		int[] g1vec = g1.getTimeStamp();
		int[] g2vec = g2.getTimeStamp();
		int biggercounter = 0;
		int smallercounter = 0;
		// int equalcounter = 0;
		if (g1vec.length != g2vec.length) {
			System.out.println("not match!");
			return 0;
		}

		// judge bigger
		for (int i = 0; i < g1vec.length; i++) {
			if (g1vec[i] > g2vec[i]) {
				biggercounter++;
			}
			if (g1vec[i] < g1vec[i]) {
				smallercounter++;
			}
		}

		if (biggercounter > 0 && smallercounter == 0) {
			return 1;
		} else if (smallercounter > 0 && biggercounter == 0) {
			return -1;
		} else {
			return 0;
		}
	}

}
