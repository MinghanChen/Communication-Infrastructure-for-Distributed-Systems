package multicast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import messagepasser.MessagePasser;

public class RMulticast {
	String groupName = "";
	List<Group> groups;
	MessagePasser mp;
	Queue<GroupStampedMessage> deliverqueue;
	HashMap<String, Queue<GroupStampedMessage>> hashmap;

	// ClockService clock = null;

	public RMulticast(MessagePasser mp, List<Group> groups,
			HashMap<String, Queue<GroupStampedMessage>> hashmap) {
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

	public Queue<GroupStampedMessage> getdeliver() {
		return this.deliverqueue;
	}

	public void multicastMsg(GroupStampedMessage groupstampmess, Group thegroup)
			throws NumberFormatException, UnknownHostException, IOException {

		List<String> members = thegroup.getNames();
		if (groupstampmess.getSource().equals(this.mp.getName())) {
			this.mp.increase();
			for (String dest : members) {
				groupstampmess.set_dest(dest);
				this.mp.sendMul(groupstampmess);
				if (members.indexOf(dest) == members.size() - 1)
					break;

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

			boolean hasMsg = false;
			for (GroupStampedMessage gmsg : hashmap.get(groupname)) {
				if (gmsg.getSource().equals(tmsg.getSource())
						&& groupname.equals(gmsg.get().groupName)) {
					int[] garray1 = gmsg.getTimeStamp();
					int[] garray2 = tmsg.getTimeStamp();
					boolean flag = true;
					if (garray1.length == garray2.length)
						for (int i = 0; i < garray1.length; i++) {
							if (garray1[i] != garray2[i]) {
								flag = false;
								break;
							}
						}
					if (flag) {
						hasMsg = true;
						break;
					}
				}
			}
			if (hasMsg)
				continue;

			synchronized (hashmap.get(groupname)) {
				synchronized (deliverqueue) {
					hashmap.get(groupname).add(tmsg);
					System.out.println(tmsg.getData());
					if (tmsg.getSource().equals(this.mp.getName()))
						continue;
					hashmap.get(groupname).notifyAll();
				}

			}
			this.multicastMsg(tmsg, thegroup);
		}

	}
}
