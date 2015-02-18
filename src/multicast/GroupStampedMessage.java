package multicast;

import java.io.Serializable;

import messagepasser.TimeStampedMessage;

public class GroupStampedMessage extends TimeStampedMessage implements Serializable{

	private static final long serialVersionUID = 1L;
	int[] grouparray;
	int size;
	Group thegroup;
	
	boolean isVisted = false;
	
	public GroupStampedMessage(String dest, String kind, Object data, boolean isSendtoLogger,
								int size, Group thegroup) {
		super(dest, kind, data, isSendtoLogger);
		grouparray = new int[size];
		this.size = size;
		this.thegroup = thegroup; 
	}
	
	public void setGroupTimeStamp(int[] timestamp) {
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
		System.out.println(super.toString());
		System.out.println("The group timestamp is : " + arraytoString());
	}
	
	public void setVisited() {
		this.isVisted = true;
	}
}
