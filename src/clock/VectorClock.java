package clock;

public class VectorClock implements ClockService{
	int[] timeStamp;
	int position;
	
	public VectorClock(int number, int position){
		timeStamp = new int[number];
		this.position = position;
	}
	public void increase() {
		timeStamp[position]++;		
	}

	public void compare(int[] newVec) {
		for(int i = 0 ; i < timeStamp.length; i++){
			if(newVec[i] > timeStamp[i])
				timeStamp[i] = newVec[i];				
		}
		this.increase();
		
	}
	
	public int[] getTimeStamp(){
		return this.timeStamp;
	}
}
