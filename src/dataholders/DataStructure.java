package dataholders;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class DataStructure {
	/*	PLANNED UPDATES
	 **** 	- change array data type to an arrayList (this provides additional flexibility of not having to define array length at creation time
	 ****  - create a variable that holds a location for use when displaying data (consider using PVector)
	 ****  - create a function that reads the data into the array list 
	 *  - create adjustment functions that (1) average out the data for each second; (2) normalizes the data by removing readings that distort visualization
	 **** create a draw function that displays the data (we may want to create more than one draw function, e.g. one with a location offset) 
	 *  - convert values into real world metrics (e.g. distance, lumens, excitement)
	 */

	// data storage variables
	public String fieldName;
	protected ArrayList<Integer> origValues;			// holds the original values that were read from the file
	public ArrayList<Integer> adjValues;				// holds the adjusted values that were calculated as averages
	public ArrayList<PVector> relativeLocations;			// hold location of original values
	public ArrayList<PVector> absoluteLocations;				// hold location of adjusted values
	protected ArrayList<Integer> peaks;
	protected ArrayList<Integer> valleys;

	// display variables
	public int myColor = 0;								
	public PVector dataFieldPosition;
	public static boolean dataDisplayBottom = false;			// set to true when data bars located at bottom of their allocated space
	public static float dataBarHeightPercent = 0.9f;			// set the height of the data bars as percent of their allocated space
	public static float textDisplayLocationPercent = 0.07f;	// set the text display location as percent of their allocated space 
	public static float dataLegendPosition = 0;
	public int fieldHeight;
	public int fieldNumber;

	// normalization normalization variables
	public int origBiggestValueEver = 0;						
	public int adjBiggestValueEver = 0;
	public int origSmallestValueEver = Integer.MAX_VALUE;
	public int adjSmallestValueEver = Integer.MAX_VALUE;
	
	// peak/valley identification variables
	public int lastReading = 0;
	public int consecutiveThreshold = 15;
	public int lastTransition = 0;
	public int consecutiveDown = 0;
	public int consecutiveUp = 0;
	public int lastMark;

	
	public DataStructure(String _fieldName, int _color, int xloc, int yloc, int _lineThickness, int _height, int _fieldNumber) {
		fieldName = _fieldName;
		fieldNumber = _fieldNumber;
		fieldHeight = _height;
		myColor = _color;
		dataFieldPosition = new PVector(xloc, yloc);
		origValues = new ArrayList<Integer>();
		adjValues = new ArrayList<Integer>();
		valleys = new ArrayList<Integer>();
		peaks = new ArrayList<Integer>();
		relativeLocations = new ArrayList<PVector>();
		absoluteLocations = new ArrayList<PVector>();
		PApplet.println("Field Name: " + fieldName + " Field Number " + fieldNumber);
	}

	public DataStructure(String _fieldName, int _color, int _height, int _fieldNumber) {
		fieldName = _fieldName;
		fieldNumber = _fieldNumber;
		fieldHeight = _height;
		myColor = _color;
		origValues = new ArrayList<Integer>();
		adjValues = new ArrayList<Integer>();
		valleys = new ArrayList<Integer>();
		peaks = new ArrayList<Integer>();
		PApplet.println("Field Name: " + fieldName + " Field Number " + fieldNumber);
	}

	
	public void setOverallLoc(float x, float y) {
		dataFieldPosition = new PVector(x, y);
	}
	
	public void setValue(int _readingNumber, int _reading) {
		origValues.add(_reading);													// load value into the array
		adjValues.add(_reading);													// load value into the array
		relativeLocations.add(new PVector(-100,-100));										// add item to the location array
		absoluteLocations.add(new PVector(-100,-100));											// add item to the location array
		if (_reading < origSmallestValueEver) origSmallestValueEver = _reading; 	// check if this is the smallest value that has been read in
		if (_reading > origBiggestValueEver) origBiggestValueEver = _reading; 		// check if this is the largest value read in
		adjSmallestValueEver = origSmallestValueEver;
		adjBiggestValueEver = origBiggestValueEver;
		checkForPeak(_readingNumber, _reading);
	}

	public void adjustBigSmallValues() {
		adjSmallestValueEver = Integer.MAX_VALUE;		// re-initialize the adjusted smallest value ever variable
		adjBiggestValueEver = 0;						// re-initialize the adjusted largest value ever variable
		peaks = new ArrayList<Integer>();				// re-initialize the peaks array			
		valleys = new ArrayList<Integer>();				// re-initialize the valleys array
		for (int i = 0; i < adjValues.size(); i++ ) {
			if (adjValues.get(i) < adjSmallestValueEver) adjSmallestValueEver = adjValues.get(i); 	// check if this is the smallest value that has been read in
			if (adjValues.get(i) > adjBiggestValueEver) adjBiggestValueEver = adjValues.get(i);		// check if this is the largest value read in
			checkForPeak(i, adjValues.get(i));
		}
	}
	
	public void averageValue(ArrayList<Integer> _indexList) {
		ArrayList<Integer> newValues;
		newValues = new ArrayList<Integer>();
		for(int i = 0; i < (_indexList.size()-1); i++) {										// loop through each element of the indexList array
			int avValueCalc = 0;
			int avValueNum = 0;
			for(int avLoop = _indexList.get(i) ; avLoop < _indexList.get(i+1); avLoop++) {		// loop through each value in array to calculate average value
				avValueCalc = avValueCalc + origValues.get(avLoop);
				avValueNum++;
			}
			newValues.add(avValueCalc/avValueNum);
		}
		adjValues = newValues;
		adjustBigSmallValues();
		resetOrigValue();
	}
	
	public void resetOrigValue() {
		origValues = adjValues;
		origSmallestValueEver = adjSmallestValueEver;
		origBiggestValueEver = adjBiggestValueEver;
	}

	public void setRelativeLocation(int _index, PVector _location){
		if (relativeLocations.size() > _index) {
			relativeLocations.set(_index, new PVector(_location.x, _location.y));			
		}
	}

	public void calculateAbsoluteLocation(float multiplierX, float multiplierY, float offsetX, float offsetY) {
		for (int i = 0; i < relativeLocations.size(); i++) {
			PVector originalLocation = relativeLocations.get(i);
			PVector absoluteLocation = absoluteLocations.get(i);
			if(originalLocation.x > -1 && originalLocation.y > -1) {
				absoluteLocation.x = offsetX + (originalLocation.x * multiplierX);
				absoluteLocation.y = offsetY + (originalLocation.y * multiplierY);
			}		
		}
	}


	/**********************
	 * BEGIN GET FUNCTIONS 
	 * Functions that provide information regarding data contained in this object 
	 **********************/
	public int getSize(){
		return adjValues.size();
	}

	public int getValue(int _readingNumber){
		int value = (Integer) adjValues.get(_readingNumber);
		return value;
	}

	public int getColor(){
		return myColor;
	}

	public PVector getAbsoluteLocation (int _readingNumber) {
		return absoluteLocations.get(_readingNumber);
	}

	public PVector getRelativeLocation (int _readingNumber) {
		return relativeLocations.get(_readingNumber);
	}
	
	/**********************
	 * END GET FUNCTIONS 
	 * Functions that provide information regarding data contained in this object
	 *********************** */
	
	
	public void checkForPeak(int _readingNumber, int _reading) {

		// check if current reading is higher or lower, and then adjust the consecutiveUp and consecutiveDown variables
		int difference = _reading - lastReading;
		if (difference < 0) {
			consecutiveDown++;
			consecutiveUp = 0;
		}
		if (difference > 0) {
			consecutiveDown = 0;
			consecutiveUp++;
		}

			// Check for exact valley location once a peak trend is detected 
		if (lastTransition <= 0 && consecutiveUp > consecutiveThreshold) {		// if last trend captured was towards a valley (lastTransition == 0) and we see a new trend towards a peak
			lastTransition = 1; 												// set lastTransition variable to 1 (meaning we've identified a peak trend)
			int valleyValue = Integer.MAX_VALUE;									
			int valleyPos = 0;
			for (int i = _readingNumber; i > lastMark; i--) {		// loop from current reading number to last peak/valley location (lastMark)
				int value = adjValues.get(i);
				if (value < valleyValue) {							// if value is lower than valleyValue then  
					valleyValue = value;								// set valleyValue to value 
					valleyPos = i;										// and set ValeyPos to current loop number
				}
			}
			valleys.add(valleyPos);						// mark the valley identified in the loop above
			lastMark = _readingNumber;					// re-initialize the index position of last peak/valley location (lastMark)
		}

		// Check for exact peak location once a valley trend is detected 
		if (lastTransition >= 0 && consecutiveDown > consecutiveThreshold) { 	// if last trend captured was towards a peak (lastTransition == 1) and we see a new trend towards a valley
			lastTransition = -1;												// set lastTransition variable to 1 (meaning we've identified a valley trend)
			int peakValue = 0;
			int peakPos = 0;
			for (int i = _readingNumber; i > lastMark; i--) {		// loop from current reading number to last peak/valley location (lastMark)
				int value = adjValues.get(i);
				if (value > peakValue) {							// if value is lower than valleyValue then 
					peakValue = value;									// set peakValue to value 
					peakPos = i;										// and set ValeyPos to current loop number
				}
			}
			peaks.add(peakPos);							// mark the valley idetnfied in the loop above
			lastMark = _readingNumber;					// re-initialize the index position of last peak/valley location (lastMark)
		}

		lastReading = _reading;			// save last reading in order to check if values are going up or down
	}


	// returns whether a reading is a valley or a peak, based on the array index number
	public int isAPeakOrValley(int _placeInArray) {
		for (int i = peaks.size()-1; i >= 0; i--) {
			int peak = peaks.get(i);
			if (peak == _placeInArray) return 1;
		}
		for (int i = valleys.size()-1; i >= 0; i--) {
			int valley = valleys.get(i);
			if (valley == _placeInArray) return -1;
		}
		return 0;
	}

	/**********************
	 * BEGIN DRAW FUNCTIONS 
	 * Functions that draw data to screen
	 *********************** */
	
	public void drawField(int dataDisplayOffset, int dataDisplayEnd) {
	}
		
}
// END SUB-CLASS DEFINITION: DataFields

