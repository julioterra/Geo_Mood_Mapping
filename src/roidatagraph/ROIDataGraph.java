package roidatagraph;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.io.*;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.Static;

import processing.core.PFont;
import dataholders.GpsData;
import dataholders.DataStructure;
import dataholders.GpsDataArray;
import dataholders.StaticData;
import dataholders.TimeStructure;

public class ROIDataGraph extends PApplet {

	// Variables that hold data from file
	TimeScale timeScale; 			// array of type long that holds the time of each reading from file
	ArrayList <Boolean> badData;	// array of type boolean that holds whether a reading contains bad data
	DataField[] fields;				// array of type DataField that holds the data for each reading
	GpsDataArray gpsActivityData;
	ArrayList<Boolean> badActivityData;
	long dataInStartTime;			// time when readings began (to adjust relative millis from arduino)
	long gpsDataTimeLapse;			// amount of time between each gps reading

	// Variables that hold display specs
	PFont font;															// font variable for displaying data to screen
	PImage nycMap;
	
	/*********
	 * START - STANDARD PROCESSING FUNCTIONS (set-up and draw
	 *********/

	// SET-UP FUNCTION: Read data from file, load and analyze
	public void setup() {

		// prepare the display
		nycMap = loadImage("nyc-map.png");
		size(nycMap.width, nycMap.height); 			
		background(StaticData.backColor);

		dataInStartTime = 0;			// time when readings began (to adjust relative millis from arduino)
		gpsDataTimeLapse = 10000l;				// amount of time between each gps reading

		font = createFont("HelveticaNeue-CondensedBold-36.vlw", 36);
		nycMap = loadImage("nyc-map.png");

		timeScale = new TimeScale(dataInStartTime);
		badData = new ArrayList<Boolean>();
		gpsActivityData = new GpsDataArray();
		badActivityData = new ArrayList<Boolean>();
  		
		StaticData.barThickness = 4;
		StaticData.currentViewPosition = 0;
		StaticData.timeScaleLocation = 220;
		StaticData.dataLocation = StaticData.timeScaleLocation + 20;
		StaticData.navLocation = 0;
		StaticData.navHeight = 40;
		StaticData.navMargin = 20;
		StaticData.startTime = 0;
		StaticData.endTime = 0;

		String mainFilename = "/Users/julioterra/Documents/ITP/2010_(2)_Fall/expression-frameworks/data/Geo_GSR_Map/gsr-log.txt";   // name of data file that will be read
		String mainDelimiter = ",";						// delimiter to be used for chopping up data
		int adjResolution = 50000;
		String gpsFilename = "/Users/julioterra/Documents/ITP/2010_(2)_Fall/expression-frameworks/data/Geo_GSR_Map/gps-coords.txt";   // name of data file that will be read
		String gpsDelimiter = ",";						// delimiter to be used for chopping up data
		
		loadMainData(mainFilename, mainDelimiter, adjResolution);			// LOAD DATA from TIME & GSR FILE
		gpsActivityData = loadData(gpsFilename, gpsDelimiter);				// LOAD DATA from GPS FILE
		gpsActivityData.adjTimeAndLoc();									// ADJUST TIME on GPS
		mergeData();
		drawIt(0,0);														// DISPLAY DATA: Call drawIt function to display graph	
	} // END SET-UP
	
	
	// DRAW FUNCTION
	public void draw() {
		drawIt(mouseX, mouseY);
	} // END DRAW FUNCTION

	/*********
	 * END - STANDARD PROCESSING FUNCTIONS (set-up and draw
	 **************************************************/

	
	
	/**************************************************
	 * START - DATA LOAD FUNCTIONS (set-up and draw
	 *********/	
	// LOAD MAIN DATA FUNCTION: function that loads the data from the main file
	public void loadMainData(String filename, String delimiter, int adjResolution) {
	/* NOTES:
	 * filename needs to include full path in order for BufferedReader to work
	 * the first line of data should include headers with titles for each field
 	 * if StaticData.timeStamped is set to false then adjResolution setting is ignored
 	 * if adjResolution is set to 0 or less then no time resolution adjustment is created
 	 * if data is StaticData.timeStamped is set to true then a TimeScale object is updated along with the fields array
	 */
		
		// Variables that hold file specs
		BufferedReader file2read;
		String currentLine;
		int totalReadings = 0;
		int validReadings = 0;						// Counter that holds number of valid readings
		int[] colors = { 0xff8a0000, 0xff009999, 0xffFF9900, 0xffffff00, 0xffff00ff, 0xff00ffff };   // array holds 6 colors
		
		// LOAD FILE IN A TRY / CATCH STATEMENT
		try{

   		// create bufferReader object and read first line of the datafile
			file2read = new BufferedReader (new FileReader(filename));
		
		// READ FIELD NAMES (first line of the data file): use field names and number to prepare data variables for reading file
			currentLine = file2read.readLine();
			String[] fieldNames = currentLine.split(delimiter);		// Read field names (assuming data located on first entry of each field)
			int numberOfFields = fieldNames.length;
			if (StaticData.timeStamped) numberOfFields--; 				// If data includes time stamp then reduce number of fields variable
			
		// INITIALIZE ARRAY: create the fields array and the dataField objects that it contains
			fields = new DataField[numberOfFields];			// Initialize fields array based on number of fields from the data	
			for (int fieldNumber = 0; fieldNumber < numberOfFields; fieldNumber++) {
				int sectionHeight = height/(numberOfFields+2);
				int yLocation = StaticData.dataLocation;
				int xLocation = 0;
				if (!StaticData.timeStamped) fields[fieldNumber] = new DataField(fieldNames[fieldNumber], colors[fieldNumber], xLocation, yLocation, StaticData.barThickness, sectionHeight, StaticData.displayOrder[fieldNumber]);		// if first field is not a timescale then start reading here
				else fields[fieldNumber] = new DataField(fieldNames[fieldNumber+1], colors[fieldNumber], xLocation, yLocation, StaticData.barThickness, sectionHeight, StaticData.displayOrder[fieldNumber]);				// otherwise, start at one field over
			}
	
		// DEBUG - PRINT TO CONSOLE: Print to message window number of entries and fields, and field names
			if (StaticData.timeStamped) println("Attempting to load data with " + numberOfFields + " fields plus a timestamp"); 
			else println("Attempting to load data with " + numberOfFields + " fields. Data has no timestamp"); 
			
		// READ DATA - read first line of data then loop through each additional life of the data file
		  currentLine = file2read.readLine();
		  while(currentLine != null) {
				totalReadings++;
				String[] fieldValues = currentLine.split(delimiter);
				// Check to make sure proper number of fields are present in each line
				if (fieldValues.length != fieldNames.length) {		
					badData.add(true);
					System.out.println("Bad number of fields on entry: " + totalReadings + " - " + currentLine);
					continue;  // jumps to next item in the dataLines array by skipping instructions below
				}
				// Loop through each data in each record and assign it to appropriate field object 
				for (int fieldNumber = 0; fieldNumber < fieldValues.length; fieldNumber++) {
					int thisValue = 0;
					try {
						// if data includes time stamp and this is the first field then record the time into the time array
						if (StaticData.timeStamped && fieldNumber == 0) { 
							long readTime = Long.parseLong(fieldValues[0].trim());
							timeScale.add(readTime); 
						}

						else {
							// record the value into the object for that field.
							thisValue = Integer.parseInt(fieldValues[fieldNumber].trim());	// convert value into an integer
							int whichField = fieldNumber;									// create variable to offset field number due to presence of time stamp
							if (StaticData.timeStamped) whichField--;						// offset field number if time stamp flag set to 
							fields[whichField].setValue(totalReadings-1, thisValue);		// add new value to field object at appropriate location
						}
					} catch (NumberFormatException e) {										// catch errors associated with converting string to integer value
						System.out.println("Couldn't parse line " + totalReadings + " " + currentLine);
						badData.add(true);
						continue;								// jump to process next field
					} catch (Exception e) {
						System.out.println("Something bad " + totalReadings + " " + currentLine + e);
						badData.add(true);
						continue;								// jump to process next field
					}
				}
				badData.add(false);
				validReadings++; 		// increase count of valid readings
			
				// ADJUST DATA RESOLUTION: every 50,000 readings (in case we have too many readings per second - for timestamped data only)
				if (StaticData.timeStamped && adjResolution > 0) {
					if (validReadings%adjResolution == 1) {
						ArrayList<Integer> averageIndex = timeScale.avgTime(StaticData.defaultTimeResolution);
						timeScale.resetOrigTime();
						for (int fieldNumber = 0; fieldNumber < fields.length; fieldNumber++) {
							fields[fieldNumber].averageValue(averageIndex);
							fields[fieldNumber].resetOrigValue();
						}
					}	
				}
				// load next line from the data file
				currentLine = file2read.readLine();
		  } // END While loop
		} catch(Exception e){
			println("error reading titles from file - more info: " + e + e.getMessage()); 
		}
		
		// FINAL ARRAY ADJUSTMENT - if data is time stamped then adjust array based on standard time resolution as set by default variable		
		if (StaticData.timeStamped && adjResolution > 0) {
			ArrayList<Integer> averageIndex = timeScale.avgTime(StaticData.defaultTimeResolution);
			timeScale.resetOrigTime();
			for (int fieldNumber = 0; fieldNumber < fields.length; fieldNumber++) {
				fields[fieldNumber].averageValue(averageIndex);
				fields[fieldNumber].resetOrigValue();
			}
		}	

		// DEBUG print stats regarding the data
		println("Number of valid readings equals: " + validReadings);	// number of valid readings
		for (int i = 0; i < fields.length; i++) {							// name of each field along with min and max values
			DataField thisField = fields[i];								
			println("Readings from " + thisField.fieldName + " range from " + thisField.origSmallestValueEver + " to " + thisField.origBiggestValueEver);
		}

	} // END LoadMainData Function

	
	public GpsDataArray loadData(String _filename, String _delimiter) {
		/* NOTES:
		 * filename needs to include full path in order for BufferedReader to work
		 * the first line of data should include headers with titles for each field
		 */
		
		// Variable that is returned by the function
		GpsDataArray gpsDataArray;
		gpsDataArray = new GpsDataArray();

		// Variables that hold file specs
		BufferedReader file2read;
		String currentLine;
		int totalReadings = 0;
		int validReadings = 0;						// Counter that holds number of valid readings

		// LOAD FILE IN A TRY / CATCH STATEMENT
		try{

			// create bufferReader object and read first line of the datafile
			file2read = new BufferedReader (new FileReader(_filename));
		
			// READ FIELD NAMES (first line of the data file): use field names and number to prepare data variables for reading file
			currentLine = file2read.readLine();
			String[] fieldNames = currentLine.split(_delimiter);		// Read field names (assuming data located on first entry of each field)
			int numberOfFields = fieldNames.length;
			
			// DEBUG - PRINT TO CONSOLE: Print to message window number of entries and fields, and field names
			println("Attempting to load data with " + numberOfFields + " fields."); 
			delay (5000);

			currentLine = file2read.readLine();
			while(currentLine != null) {
					totalReadings++;			// increase total readings count
					String[] fieldValues = currentLine.split(_delimiter);
					// Check to make sure proper number of fields are present in each line
					if (fieldValues.length != numberOfFields) {		
						badActivityData.add(true);
						System.out.println("Bad number of fields on entry: " + totalReadings + " - " + currentLine);
						currentLine = file2read.readLine();		// load next line from the data file
						println("field names" + numberOfFields + " field Value length " + fieldValues.length);
						continue;  // jumps to next item in the dataLines array by skipping instructions below
					}
					try {
						float locX = Float.parseFloat(fieldValues[1].trim());
						float locY = Float.parseFloat(fieldValues[0].trim());
						gpsDataArray.add(locX, locY);
					} catch (NumberFormatException e) {										// catch errors associated with converting string to integer value
							System.out.println("Couldn't parse line " + totalReadings + " " + currentLine);
							badActivityData.add(true);
							currentLine = file2read.readLine();		// load next line from the data file
							continue;								// jump to process next field
						} catch (Exception e) {
							System.out.println("Something bad " + totalReadings + " " + currentLine + e);
							badActivityData.add(true);
							currentLine = file2read.readLine();		// load next line from the data file
							continue;								// jump to process next field
						}
						badActivityData.add(false);
					validReadings++; 						// increase count of valid readings
					currentLine = file2read.readLine();		// load next line from the data file
			  } // END While loop
			} catch(Exception e){
				println("error reading titles from file - more info: " + e + e.getMessage()); 
			}

			// DEBUG print stats regarding the data
			println("Activity Data: Number of valid readings equals: " + validReadings);	// number of valid readings

			
		gpsDataArray.printHighLow();
		return gpsDataArray;
	}
	
	/*********
	 * END - DATA LOAD FUNCTIONS 
	 **************************************************/

	
	
	/**************************************************
	 * START - DATA ADJUSTMENT FUNCTIONS
	 *********/	
		
	// CHANGE TIME RESOLUTION FUNCTION
	// Function that enables changing the time resolution temporarily change the time resolution temporarily
	public void changeTimeResolution(long _millis) {
		StaticData.adjTimeResolution = _millis;
		ArrayList<Integer> averageIndex = timeScale.avgTime(StaticData.adjTimeResolution);
		for (int fieldNumber = 0; fieldNumber < fields.length; fieldNumber++) {
			fields[fieldNumber].averageValue(averageIndex);
		}		
	}
	// END CHANGE TIME RESOLUTION FUNCTION

	public void mergeData() {
		int gsrIndex = 0;
		float offsetX = 320;
		float offsetY = 50;
		float multiplier = 0.85f;
		float multiplierX = 0.78f * multiplier * height;
		float multiplierY = 1f * multiplier * height;

		for (int i = 0; i < gpsActivityData.numGpsLocations()-1; i++) {
			PVector startLoc = gpsActivityData.locationByIndex(i);
			PVector endLoc = gpsActivityData.locationByIndex(i+1);
			PVector coordDist = new PVector((float)(endLoc.x - startLoc.x), (float)(endLoc.y - startLoc.y));
			float startTime = gpsActivityData.timeByIndex(i);
			float endTime = gpsActivityData.timeByIndex(i+1);
			float timeSegmentDuration = (float)(endTime - startTime);
			float drawTime = timeScale.get(gsrIndex);
//			println("start location x: " + startLoc.x + " y: " + startLoc.y);
			
			while ((drawTime < endTime) && (gsrIndex < fields[0].adjValues.size())) {
				if (drawTime > startTime) {
					float percentDuration = (float)((drawTime-startTime)/timeSegmentDuration);
					float gsrLocationX = (float)((startLoc.x + (float)(coordDist.x * percentDuration)));
					float gsrLocationY = (float)((startLoc.y + (float)(coordDist.y * percentDuration)));
					fields[0].setRelativeLocation(gsrIndex, new PVector(gsrLocationX, gsrLocationY));
				}
				gsrIndex++;
				drawTime = timeScale.get(gsrIndex);
			}
			fields[0].calculateAbsoluteLocation(multiplierX, multiplierY, offsetX, offsetY);
		}
	}
	
	
	// DRAWIT FUNCTION - Display graph with data
	public void drawIt(float _xLoc, float yLoc) {
		smooth();
		background(255);
		image(nycMap, 0, 0);
		for (int i = 0; i < fields[0].getSize()-1; i++) {
			PVector currentLoc = fields[0].getAbsoluteLocation(i);
			float ballSize = fields[0].getValue(i)/11;
			noStroke();
			fill(255,0,0,60);
			if (currentLoc.x > -1 && currentLoc.y > -1) {
				ellipse(currentLoc.x, currentLoc.y,ballSize, ballSize);
			}
		}					
	}
	// END DRAWIT FUNCTION
	

	public void mousePressed() {
		StaticData.updateDisplay = true;
	}

	public void mouseReleased() {
		StaticData.updateDisplay = false;
	}
	
	
	
	/*************
	 * CREATE AND EXTEND CLASSES
	 *************/
	
	// DEFINE TIME SCALE CLASS - save time scale and process it
	public class TimeScale extends TimeStructure {
		
		public TimeScale () {
			super();
		}

		public TimeScale (long adjTimeStart) {
			super(adjTimeStart);
		}
		
		
		public void drawTimeLine(int dataDisplayOffset, int dataDisplayEnd) {
			// variables that holds where hours, minutes and seconds begin and end withing a time string
			int topTimeScaleLine = StaticData.timeScaleLocation-17;
			int bottomTimeScaleLine = StaticData.timeScaleLocation+5;
		
			// TIME STAMP DRAW
			// Draw top and bottom line around Time Stamps
			fill(StaticData.timeStampColor);
			stroke(StaticData.timeStampColor);
			strokeCap(SQUARE);
			strokeWeight(1);
			line(0, topTimeScaleLine, width, topTimeScaleLine);
			strokeWeight(1);
			line(0, bottomTimeScaleLine, width, bottomTimeScaleLine);
			textFont(font, 15);

			// Write Time Stamps to Screen
			for (int curReading = 10; curReading < timeScale.size(); curReading = curReading + 40) {
				Date d = new Date(timeScale.get(curReading));								// create date from the timestamp
				int location = 0 + ((curReading - dataDisplayOffset)*StaticData.barThickness);
				String time = d.toString();
				text(time.substring(StaticData.hoursBegin, StaticData.secondsEnd), location, StaticData.timeScaleLocation);		// display date			
			}	
		}
	} // END TimeScale Class

	
	// SUB-CLASS DEFINITION: DataField Class - Extension of FieldData class
	public class DataField extends DataStructure {
		
		public DataField(String _fieldName, int _color, int xloc, int yloc, int _lineThickness, int _height, int _fieldNumber) {
			super(_fieldName, _color, xloc, yloc, _lineThickness, _height, _fieldNumber);
		}

		public void drawField(int dataDisplayOffset, int dataDisplayEnd) {

		}
	}  // END SUB-CLASS DEFINITION: DataFields

	

}
