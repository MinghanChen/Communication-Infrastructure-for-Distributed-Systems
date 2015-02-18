package clock;

public interface ClockService {
	
	public void increase();
	
	public void compare(int[] newVec);
	
	public int[] getTimeStamp();
	
	public int getPosition();
	
	public void setBit(int index, int value);
	
}
