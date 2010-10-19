package dataholders;

import processing.core.PVector;

public class GpsData {
	long logTime;
	PVector gpsLoc;

	public GpsData(long logTime, float x, float y) {
		this.logTime = logTime;
		this.gpsLoc = new PVector(x, y);
	}

	public void setTime(long _newTime) {
		this.logTime = _newTime;
	}

	public void setLoc(float x, float y) {
		this.gpsLoc = new PVector(x, y);
	}

	public long getTime() {
		return logTime;
	}
	
	public float getLocX(){
		return gpsLoc.x;
	}
	
	public float getLocY(){
		return gpsLoc.y;
	}

}
