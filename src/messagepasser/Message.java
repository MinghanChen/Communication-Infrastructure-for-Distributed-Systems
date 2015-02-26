package messagepasser;

import java.io.Serializable;

public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String dest = "";
	private String kind = "";
	private Object data = "";
	private String source = "";
	private int sequenceNumber;
	private boolean dupe;
	private boolean isSendtoLogger;
	boolean isMulti = false;
	
	private boolean isRequest;
	private boolean isACK;
	private boolean isRelease;
	
	
	public Message(String dest, String kind, Object data, boolean isSendtoLogger) {
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.dupe = false;
		this.isSendtoLogger = isSendtoLogger;
		this.isRequest = false;
		this.isACK = false;
		this.isRelease = false;

	}
	
	public boolean getRequest() {
		return this.isRequest;
	}
	
	public boolean getACK() {
		return this.isACK;
	}
	
	public boolean getRelease() {
		return this.isRelease;
	}
	
	public void setRequest() {
		this.isRequest = true;
	}
	
	public void setRequestFalse() {
		this.isRequest = false;
	}
	
	public void setACK() {
		this.isACK = true;
	}
	
	public void setRelease() {
		this.isRelease = true;
	}
	
	public void setMulti(){
		isMulti = true;
	}
	
	public boolean getMulti(){
		return this.isMulti;
	}
	
	public void set_source(String source) {
		this.source = source;
	}
	
	public void set_dest(String destination) {
		this.dest = destination;
	}
	
	public void set_seqNum(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public void set_duplicate(Boolean dupe) {
		this.dupe = dupe;
	}
	
	public void set_isSendtoLogger(boolean isSendtoLogger) {
		this.isSendtoLogger = isSendtoLogger;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public String getDest() {
		return dest;
	}
	
	public String getData() {
		return data.toString();
	}
	
	public String getSource() {
		return source;
	}
	
	public boolean get_isSendtoLogger() {
		return isSendtoLogger;
	}
	
	public boolean getDup() {
		return dupe;
	}
	
	public int getNum() {
		return sequenceNumber;
	}
	
	public String getKind() {
		return kind;
	}
	
	@Override
	public String toString() {
		return ("The destination : " + dest + "  The source : " + source + "The kind : " + kind +
							"  The sequence number : " + sequenceNumber + "  The content is : " + data.toString());
	}
	
}
