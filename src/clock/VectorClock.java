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
	
	public int getPosition() {
		return position;
	}
	
	public int[] getTimeStamp(){
		return this.timeStamp;
	}
	
	public void setBit(int index, int value) {
		timeStamp[index] = value;
	}
	
	public void print() {
		StringBuffer sb = new StringBuffer("[ ");
		for (int i = 0; i < timeStamp.length; i++) {
			sb.append(timeStamp[i] + " ");
		}
		System.out.println(sb.append("]").toString());
	}
}
