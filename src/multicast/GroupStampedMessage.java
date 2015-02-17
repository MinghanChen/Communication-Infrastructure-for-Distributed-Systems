package multicast;

import java.io.Serializable;

import messagepasser.TimeStampedMessage;

public class GroupStampedMessage extends TimeStampedMessage implements Serializable{
	int[] grouparray;
	int size;
	Group thegroup;
	
	public GroupStampedMessage(String dest, String kind, Object data, boolean isSendtoLogger,
								int size, Group thegroup) {
		super(dest, kind, data, isSendtoLogger);
		grouparray = new int[size];
		this.size = size;
		this.thegroup = thegroup; 
	}
	
	public void setTimeStamp(int[] timestamp) {
		for (int i = 0; i < size; i++) {
			grouparray[i] = timestamp[i];
		}
	}
	
	public int[] getTimeStamp() {
		return grouparray;
	}
	
	public Group get() {
		return thegroup;
	}
	
	public String arraytoString() {
		StringBuffer sb = new StringBuffer("{ ");
		for (int i = 0; i < size; i++) {
			sb.append(grouparray[i] + " ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public String getgroupname() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < grouparray.length; i++) {
			sb.append(grouparray);
		}
		return sb.toString();
	}
	
	public void print() {
		super.toString();
		System.out.println("The group timestamp is : " + arraytoString());
	}
}
