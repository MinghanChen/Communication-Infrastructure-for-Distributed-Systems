package clock;

public interface ClockService {
	
	public void increase();
	
	public void compare(int[] newVec);
	
	public int[] getTimeStamp();
	
}
