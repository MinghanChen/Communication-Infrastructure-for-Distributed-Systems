package mutualexclusion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import yamlparser.YamlParser;
import messagepasser.MessagePasser;
import multicast.Group;
import multicast.GroupStampedMessage;
import multicast.RMulticast;

public class MultualExclusion {
	private RMulticast rMul;
	private HashMap<String, Boolean> checkAvailable = new HashMap<String, Boolean>();
	private MessagePasser mp;
	private Hashtable<String, ArrayList<String>> mutualmap;
	private List<String> members;
	private boolean status = false;
	private Group thegroup;
	
	boolean ischeck;
	
	public MultualExclusion(String configuration_filename, MessagePasser mp){
		YamlParser yamlparser = new YamlParser();
		try {
			yamlparser.yamiParser(configuration_filename);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		mutualmap = yamlparser.getMutualmap();
		members = mutualmap.get(mp.getName());
		if (members == null) {
			System.err.println("cannot find the name of the host");
		}
		
		thegroup = new Group();
		thegroup.setNames(members);
		Iterator<String> iter = members.iterator();
		while (iter.hasNext()) {
			checkAvailable.put(iter.next(), false);
		}
		
//		for(int i = 0 ; i < mutualmap.size(); i++){
//			if(megroups.get(i).getGroupName().equals(this.groupname)){
//				thegroup = megroups.get(i);
//				List<String> members = thegroup.getNames();
//				for(int j = 0 ; j < members.size(); j++)
//					checkAvailable.put(members.get(j), false);
//			}
//		}
		this.mp = mp;
		rMul = new RMulticast(this.mp);
		this.ischeck = false;
		
		new Thread(new WaitingACK()).start();
		
	}
	
	
	public void requestCritical() throws NumberFormatException, UnknownHostException, IOException{
		
//		for(int i = 0 ; i < megroups.size(); i++){
//			if(megroups.get(i).getGroupName().equals(this.groupname))
//				thegroup = megroups.get(i);
//		}
		GroupStampedMessage gmsgreq = new GroupStampedMessage("", "", "", false, 0, null);
		gmsgreq.set_source(this.mp.getName());
		gmsgreq.setRequest();
		this.rMul.multicastMsg(gmsgreq, thegroup);
		System.out.println("Waiting for critical section");
		while(!status){
			this.checkAvailability();
		}
		status = false;
		GroupStampedMessage gmsgrelease = new GroupStampedMessage("", "", "", false, 0, null);
		gmsgrelease.set_source(this.mp.getName());
		gmsgrelease.setRelease();	
		gmsgrelease.setMulti();
		this.rMul.multicastMsg(gmsgrelease, thegroup);
		synchronized(checkAvailable){
			Set<String> keyset = checkAvailable.keySet();
			Iterator<String> iter = keyset.iterator();
			while (iter.hasNext()) {
				checkAvailable.put(iter.next(), false);
			}
 		}
	}
	
	private void checkAvailability() throws IOException{
		boolean flag = true;
		synchronized(checkAvailable){
			while (!ischeck) {
				try {
					checkAvailable.wait();
				} catch (InterruptedException e) {
					System.err.println("error in checkaAvailability()");
				}
			}
			Set<String> keyset = checkAvailable.keySet();
			Iterator<String> iter = keyset.iterator();
			while (iter.hasNext()) {
			   Boolean val = checkAvailable.get(iter.next());
			   if(!val) {
				   flag = false;
				   break;
			   }
			   
			}
		}
		
		if(flag){
			this.getCriticalSection();	
			this.status = true;	
		}
		
		ischeck = false;
	}

	private void getCriticalSection() throws IOException {
		System.out.println("You got the critical section, input any word to leave ");
		//need further implementation
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();
		System.out.println("You leave the critical section");
	}
	
	private class WaitingACK implements Runnable{
		
		public void run() {
			String name;
			while (true) {		
				name = mp.getACK();
				synchronized(checkAvailable){
					if(checkAvailable.containsKey(name)) {
						checkAvailable.put(name, true);
						//System.out.println("the name of checkavailable updated : " + name);
						ischeck = true;
						checkAvailable.notifyAll();
					}
						
				}

			}
			
		}
	}
	
}
