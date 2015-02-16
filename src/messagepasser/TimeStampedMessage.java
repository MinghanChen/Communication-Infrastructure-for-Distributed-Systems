package messagepasser;

import java.io.Serializable;

public class TimeStampedMessage extends Message implements Serializable, Comparable<TimeStampedMessage>{
	
	private static final long serialVersionUID = 1373993646530018852L;
	String vector = "";
	int[] vectorArray;
	boolean isSend;
	
	public void changeisSend() {
		isSend = false;
	}
	
	public TimeStampedMessage(String dest, String kind, Object data, boolean isSendtoLogger){
		super(dest,kind,data,isSendtoLogger);
		this.isSend = true;
	}
	
	public void setTimeStamp(int[] timeStamp){
		this.vectorArray = timeStamp;
		vector = "";
		for(int i = 0 ; i < timeStamp.length; i++)
			vector = vector + Integer.toString(timeStamp[i])+" ";
	}
	
	public int[] getTimeStampVec(){
		String[] sp = vector.split(" ");
		vectorArray = new int[sp.length];
		for(int i = 0 ; i < sp.length; i++)
			vectorArray[i] = Integer.parseInt(sp[i]);
		return this.vectorArray;
	}
	
	public int compareTo(TimeStampedMessage message) {
		int[] counterpart = message.getTimeStampVec();
		int biggercounter = 0;
		int smallercounter = 0;
		//int equalcounter = 0;
		if (counterpart.length != vectorArray.length) {
			System.out.println("not match!");
			return 0;
		}
		
		//judge bigger
		for (int i = 0; i < counterpart.length; i++) {
			if (vectorArray[i] > counterpart[i]) {
				biggercounter++;
			} 
			if (vectorArray[i] < counterpart[i]) {
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
	
	@Override
	public String toString() {
			StringBuffer result = new StringBuffer();
			if (isSend) {
				result.append("vector clock of sending port :  ");
			} else {
				result.append("vector clock of receiving port :  ");
			}

			result.append("The destination : " + super.getDest() + "  The source : " + super.getSource() +
					"  The sequence number : " + super.getNum() + "  The content is : " + super.getData() +
					"  The timeStamp is : {");
			for (int i = 0; i < this.getTimeStampVec().length; i++) {
				result.append(" " + this.getTimeStampVec()[i]);
			}
			result.append(" }");
			return result.toString();
		
	}
}
