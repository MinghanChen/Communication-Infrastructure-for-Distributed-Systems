package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import messagepasser.Message;
import messagepasser.MessagePasser;
import messagepasser.TimeStampedMessage;
import multicast.COMulticast;
import multicast.Group;
import multicast.RMulticast;

public class Demo {

	private static final String PROMPT_INFO_FIRST = "> 1 for unicast, 2 for multicast";
	private static final String PROMPT_INFO = "> 1 for send, 2 for receive, 3 for exit, 4 for issue timestamp";
	
	public static void main(String[] args) {
		String localName = args[0];
		String configFilePath = args[1];
		String isLogical = args[2];
		boolean[] isFableError = new boolean[1];
		MessagePasser mp = null;
		COMulticast cm = null;
		try {
			mp = new MessagePasser(configFilePath, localName, isFableError,Integer.parseInt(isLogical));
			cm = new COMulticast(mp, configFilePath);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while(true) {
				System.out.println(PROMPT_INFO_FIRST);
				String readFromCmdFirst = br.readLine();
				if(readFromCmdFirst.trim().equals("1")){
					System.out.println(PROMPT_INFO);
					String readFromCmd = br.readLine();
					if (readFromCmd.trim().equals("1")) {
						sendMessage(br, mp);
					} else if (readFromCmd.trim().equals("2")){
						receiveMessage(br, mp);
					} else if (readFromCmd.trim().equals("3")) {
						break;
					} else if (readFromCmd.trim().equals("4")){
						issueTimeStamp(mp, Integer.parseInt(isLogical));
					}else {
						continue;
					}
				}
				else if(readFromCmdFirst.trim().equals("2")){
					COMessage(br, args[0], cm);
				}
				else{
					continue;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Finish demo.");
	}
	
	private static void sendMessage(BufferedReader br, MessagePasser mp) throws IOException {
		System.out.println(">Your destination:");
		String dst = br.readLine();
		System.out.println(">Message kind:");
		String kind = br.readLine();
		System.out.println(">Message content:");
		String content = br.readLine();
		System.out.println(">Want to send to logger ? (yes or no)");
		String sendtoLogger = br.readLine();
		Message msg = new Message(dst, kind, content, sendtoLogger.equals("yes"));
		mp.send(msg);
	}
	
	private static void issueTimeStamp(MessagePasser mp, int isLogical){
		StringBuffer result = new StringBuffer("  The timeStamp is : {");
		int[] vector = mp.getVec().getTimeStamp();
		for (int i = 0; i < vector.length; i++) {
			result.append(" " + vector[i]);
		}
		result.append("}");
		System.out.println(result.toString());
	}

	
	private static void receiveMessage(BufferedReader br, MessagePasser mp) {
		try {
			TimeStampedMessage msg = mp.receive();
			System.out.println(msg);
			System.out.println(">Want to send to logger ? (yes or no)");
			if (br.readLine().trim().equals("yes")) {
				msg.changeisSend();
				msg.setTimeStamp(mp.getVec().getTimeStamp());
				mp.sendToLogger(msg);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void COMessage(BufferedReader br, String localname, COMulticast cm) throws IOException {
		System.out.println(">Group name you want to multicast:");
		String groupname = br.readLine();
		System.out.println(">Message kind:");
		String kind = br.readLine();
		System.out.println(">Message content:");
		String content = br.readLine();
		TimeStampedMessage tmsg = new TimeStampedMessage("", kind, content, false);
		cm.setGroup(groupname);
		tmsg.set_source(localname);
		cm.coMulticast(tmsg);
	}
	
}
