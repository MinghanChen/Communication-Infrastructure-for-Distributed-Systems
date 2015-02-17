package clock;

public class LogicalClock implements ClockService{
	int timeStamp;
	
	public LogicalClock(){
	    timeStamp = 0;	
	}
	
	public void increase() {
		timeStamp = timeStamp + 1;		
	}

	public void compare(int[] newTime) {
		timeStamp = newTime[0] > timeStamp ? newTime[0] + 1 : timeStamp + 1;
	}
	
	public int[] getTimeStamp(){
		int[] result = new int[1];
		result[0] = this.timeStamp;
		return result;
	}

	public void decrease() {
		timeStamp = timeStamp - 1;
		
	}
	
	public int getPosition() {
		return 0;
	}
}
