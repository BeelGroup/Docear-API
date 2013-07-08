package util;

public class Timer {
	
private long startTime;
	
	public Timer(){
		this.startTime = System.currentTimeMillis();
	}
	
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	
	public String getTime(){
		long endTime = System.currentTimeMillis();
		long time = (endTime - this.startTime);
		return time + " ms.";
	}

}
