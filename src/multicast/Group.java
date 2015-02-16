package multicast;

import java.util.ArrayList;
import java.util.List;

public class Group {
	String groupName = "";
	List<String> names = new ArrayList<String>();
	
	public void setName(String name){
		names.add(name);
	}
	
	public void setGroupName(String groupname){
		this.groupName = groupname;
	}
	
	public void setNames(List<String> names){
		this.names = names;
	}
	
	public List<String> getNames(){
		return this.names;
	}
	
	public String getGroupName(){
		return this.groupName;
	}
	
	public int getGroupSize() {
		return names.size();
	}
	
	public int find(String name) {
		for (int i = 0; i < names.size(); i++) {
			if (name.equals(names.get(i))) {
				return i;
			}
		}
		return -1;
	}
}
