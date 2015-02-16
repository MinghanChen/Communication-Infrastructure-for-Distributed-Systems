package yamlparser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import multicast.Group;

import org.yaml.snakeyaml.Yaml;

public class YamlParser {
	
	private Hashtable<String, String> portTableInfo = new Hashtable<String, String>();
	private Hashtable<String, String> sendRuleTable = new Hashtable<String, String>();
	private Hashtable<String, String> recRuleTable = new Hashtable<String, String>();
	private List<String> namelist = new ArrayList<String>();
	private String[] loggerinfo = new String[2];
	List<Group> groups = new ArrayList<Group>();

	public void yamiParser(String file) throws IOException {
		Yaml yaml = new Yaml();
		@SuppressWarnings("unchecked")
		Map<String, Object> content = (Map<String, Object>) yaml
				.load(new FileReader(file));
		Object obj = content.get("configuration");
		ArrayList<Object> people = new ArrayList<Object>((ArrayList) obj);
		for (Object object : people) {
			@SuppressWarnings("unchecked")
			Map<String, Object> person = (Map<String, Object>)object;
			String name = person.get("name").toString();
			String ip = person.get("ip").toString();
			String port = person.get("port").toString();
			this.namelist.add(name);
			try {
				int i = Integer.parseInt(port);
			} catch(NumberFormatException e) {
				System.out.println("wrong port number given!");
				continue;
			}
			
			//portTableforip.put(ip, name);
			//System.out.println(portTableforip.containsValue("CMH"));
			portTableInfo.put(name, ip + "\t" + port);
			//portTableInfo2.put(ip + "\t" + port, name);
		}
		//System.out.println(portTable.get("bob"));
	
		Object obj2 = content.get("sendRules");
		ArrayList<Object> sendrules = new ArrayList<Object>((ArrayList)obj2);
		for (Object object : sendrules) {
			Map<String, Object> rule = (Map<String, Object>)object;
			ArrayList<Object> rulelist = new ArrayList<Object>();
			rulelist.add(rule.get("src"));
			rulelist.add(rule.get("dest"));
			rulelist.add(rule.get("kind"));
			rulelist.add(rule.get("seqNum"));
			//rulelist.add(rule.get("action"));
			
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < rulelist.size(); i++) {
				if (rulelist.get(i) != null) {
					sb.append(rulelist.get(i).toString());
				}
			}
			sendRuleTable.put(sb.toString(), rule.get("action").toString());
		}
		
		Object obj3 = content.get("receiveRules");
		ArrayList<Object> receiverules = new ArrayList<Object>((ArrayList)obj3);
		//System.out.println("the number of receiverules" + receiverules.size());
		for (Object object : receiverules) {
			Map<String, Object> rule = (Map<String, Object>)object;
			ArrayList<Object> rulelist = new ArrayList<Object>();
			rulelist.add(rule.get("src"));
			rulelist.add(rule.get("dest"));
			rulelist.add(rule.get("kind"));
			rulelist.add(rule.get("seqNum"));
			//rulelist.add(rule.get("action"));
			
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < rulelist.size(); i++) {
				if (rulelist.get(i) != null) {
					sb.append(rulelist.get(i).toString());
				}
			}
			recRuleTable.put(sb.toString(), rule.get("action").toString());
		}
		
		Object obj4 = content.get("logger");
		ArrayList<Object> loggerinfo = new ArrayList<Object>((ArrayList)obj4);
		for (Object object : loggerinfo) {
			Map<String, Object> log = (Map<String, Object>)object;
			//System.out.println("the size of it : " + log.size());
			String ip = log.get("ip").toString();
			String port = log.get("port").toString();
			this.loggerinfo[0] = ip;
			this.loggerinfo[1] = port;
		}
		
		Object obj5 = content.get("groups");
		ArrayList<Object> groupinfo = new ArrayList<Object>((ArrayList)obj5);
		for (Object object : groupinfo) {
			Map<String, Object> group = (Map<String, Object>)object;
			//System.out.println("the size of it : " + log.size());
			String groupname = group.get("name").toString();
			List<String> members = (List<String>) group.get("members");
			Group g = new Group();
			g.setGroupName(groupname);
			g.setNames(members);
			groups.add(g);
		}
		
		//System.out.println(recRuleTable.size());
	}
	
	public Hashtable<String, String> getPortTableInfo() {
		return portTableInfo;
	}
	
	public Hashtable<String, String> getSendRuleTable() {
		return sendRuleTable;
	}
	
	public Hashtable<String, String> getRecRuleTable() {
		return recRuleTable;
	}
	
	public List<String> getNames(){
		return namelist;
	}
	
	public List<Group> getGroups(){
		return groups;
	}
	
	public String[] getLoggerInfo() {
		return loggerinfo;
	}

}