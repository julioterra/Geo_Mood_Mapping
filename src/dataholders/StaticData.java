package dataholders;

import processing.core.PFont;

public class StaticData {
	
	public static boolean updateDisplay = false;
	public static boolean timeStamped = true;			// flag that identifies whether the file being has time stamps 

	// Variables that hold timescale-related specs
	public static long defaultTimeResolution = 5000;							// time resolution in milliseconds that will be used as standard 
	public static long adjTimeResolution = defaultTimeResolution;
	public static long startTime;
	public static long endTime;
	public static int timeScaleLocation;
	public static int backColor = 0xffffffff;
	public static int timeStampColor = 0xff888888;
	public static int fieldTitleColor = 0xffdddddd;

	// Variables that extract specific data from java date format
	public static int hoursBegin = 11;
	public static int hoursEnd = 13;
	public static int minutesBegin = 14;
	public static int minutesEnd = 16;
	public static int secondsBegin = 17;
	public static int secondsEnd = 19;
	public static int timeTextHeight = 190;

	// Variables that hold display specs
	PFont font;															// font variable for displaying data to screen
	public static int barThickness;											// holds the thickness of the bars on the data graphs
	public static int currentViewPosition;										// holds current position selected on navigation displayed
	public static int navLocation;
	public static int navHeight;
	public static int navMargin;
	public static int dataLocation;
	public static int[] displayOrder = {1, 2, 3};						// array that holds the order in which the fields should be displayed

}
