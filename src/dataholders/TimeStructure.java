package dataholders;

import java.util.ArrayList;

public class TimeStructure {
	// DEFINE TIME SCALE CLASS - save time scale and process it
		public ArrayList<Long> origTimes;
		public ArrayList<Long> adjTimes;
		long adjTimeStart;
		
		public TimeStructure () {
			origTimes = new ArrayList<Long>();
			adjTimes = new ArrayList<Long>();
			this.adjTimeStart = 0;
		}

		public TimeStructure (long adjTimeStart) {
			origTimes = new ArrayList<Long>();
			adjTimes = new ArrayList<Long>();
			this.adjTimeStart = adjTimeStart;
		}

		
		public void add(long _time) {
			if (origTimes.size() == 0) {
				StaticData.startTime = _time + this.adjTimeStart;
			}
			origTimes.add(_time + this.adjTimeStart);
			adjTimes.add(_time + this.adjTimeStart);
			StaticData.endTime = _time;
		}
		
		public long get(int _index) {
			return adjTimes.get(_index);
		}
	
		public int size() {
			return adjTimes.size();
		}

		public void resetOrigTime () {
			origTimes = adjTimes;
		}		

		public ArrayList<Integer> avgTime(long _timeMillisecs) {
			ArrayList<Long> newTimes;
			ArrayList<Integer> indexList; 						// create arraylist that will hold index location at appropriate time phases
			newTimes = new ArrayList<Long>();
			indexList = new ArrayList<Integer>();		
			newTimes.add(origTimes.get(0)); 					// set currentTime to the first time stamp from the data file
			for(int i = 1; i < origTimes.size(); i ++) {			// loop through each element of the times array to locate time phases 
				long oldTime = newTimes.get(newTimes.size()-1);
				long newTime = origTimes.get(i);					// get next time stamp from array
				if ((newTime-oldTime) > _timeMillisecs) {	// check if sufficient number of milliseconds has passed to begin new phase
					newTimes.add(newTime);
					indexList.add(i);								// add index number to indexList
				}
			}
			adjTimes = newTimes;
			return indexList;
		}		

		public void drawTimeLine(int dataDisplayOffset, int dataDisplayEnd) {
		}
	
}
