package multicast;

import java.io.Serializable;

import messagepasser.TimeStampedMessage;

public class GroupStampedMessage extends TimeStampedMessage implements Serializable{
	int[] grouparray;
	int size;
	String groupname;
	
	public GroupStampedMessage(String dest, String kind, Object data, boolean isSendtoLogger,
								int size, String groupname) {
		super(dest, kind, data, isSendtoLogger);
		grouparray = new int[size];
		this.size = size;
		this.groupname = groupname; 
	}
	
	public void setTimeStamp(int[] timestamp) {
		for (int i = 0; i < size; i++) {
			grouparray[i] = timestamp[i];
		}
	}
	
	public int[] getTimeStamp() {
		return grouparray;
	}
	
	public String get() {
		return groupname;
	}
	
	public String getgroupname() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < grouparray.length; i++) {
			sb.append(grouparray);
		}
		return sb.toString();
	}
}
