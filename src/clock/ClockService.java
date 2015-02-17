package clock;

public interface ClockService {
	
	public void increase();
	
	public void decrease();
	
	public void compare(int[] newVec);
	
	public int[] getTimeStamp();
	
	public int getPosition();
	
}
