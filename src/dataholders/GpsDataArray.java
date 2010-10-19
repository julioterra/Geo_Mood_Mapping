package dataholders;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class GpsDataArray{
	public ArrayList<GpsData> gpsData;
	public long readTimeInterval;
	public float highX;
	public float lowX;
	public float highY;
	public float lowY;
	public float rangeX;
	public float rangeY;
	
	public GpsDataArray() {
		super();
		this.gpsData = new ArrayList<GpsData>();
	}

	public void add(float x, float y) {
		long logTime = 0;
		gpsData.add(new GpsData(logTime, x, y));
		checkHighLow(x, y);
	}

	public void add(long logTime, float x, float y) {
		gpsData.add(new GpsData(logTime, x, y));
		checkHighLow(x, y);
	}

	public PVector checkLoc(long _actTime) {
		for (int i = 0; i < gpsData.size(); i++) {
			GpsData checkPos = gpsData.get(i);
			if (checkPos.getTime() - (readTimeInterval/2) > _actTime && checkPos.getTime() + (readTimeInterval/2) < _actTime) {
				return new PVector (checkPos.getLocX(), checkPos.getLocY());
			}	
		}
		return new PVector(-1, -1);
	}
	
	public void checkHighLow(float x, float y) {
		if (gpsData.size() <= 1) {
			highX = x; lowX = x;
			highY = y; lowY = y;
		} 
		if (x < (lowX)) lowX = x;
		else if (x > (highX)) highX = x;
		if (y < (lowY)) lowY = y;
		else if (y > (highY)) highY = y;	
	}
	
	public void printHighLow() {
		System.out.println("high X " + highX + " low X " + lowX + " high Y " + highY + " low Y " + lowY);
	}
	
	public int numGpsLocations() {
		return gpsData.size();
	}
	
	public PVector locationByIndex(int index) {
		GpsData checkPos = gpsData.get(index);
		return new PVector(checkPos.getLocX(), checkPos.getLocY());		
	}

	public long timeByIndex(int index) {
		GpsData checkPos = gpsData.get(index);
		return checkPos.getTime();
	}
	
	
	public void adjTimeAndLoc() {
		long individualInterval = (StaticData.endTime - StaticData.startTime) / gpsData.size();
		rangeX = PApplet.abs(PApplet.abs(highX) - PApplet.abs(lowX));
		rangeY = PApplet.abs(PApplet.abs(highY) - PApplet.abs(lowY));
		for (int i = 0; i < gpsData.size(); i++) {
			GpsData gpsReading = gpsData.get(i);
			gpsReading.setTime(StaticData.startTime + (individualInterval * (i + 1))); 
			float tempX = PApplet.abs(highX)-PApplet.abs(gpsReading.getLocX());
			float tempY = PApplet.abs(highY)-PApplet.abs(gpsReading.getLocY());
			if (rangeX > rangeY) { gpsReading.setLoc(tempX/rangeX, tempY/rangeX); }
			else if (rangeY > rangeX) { gpsReading.setLoc(tempX/rangeY, tempY/rangeY); }
		}
		// high X -73.99018 low X -74.01852 high Y 40.75323 low Y 40.70209
		// for X take high limit and minus all values, for Y take high limit and minus all values from 	
	}
	
}
