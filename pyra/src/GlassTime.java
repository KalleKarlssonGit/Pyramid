import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Bryngel
 *
 */
public class GlassTime {

	final static double TIME_TO_FILL_TOP_GLASS_SECONDS = 10.0;

	final static int MAX_INDEX = 50;

	/*
	 * glassPyramid is a pyramid structure as described in description pdf, and is used for storing calculated values.
	 *
	 * Will be filled by glasses as they have been filled by water. Until then, the glass is null at any position.
	 *
	 * glassPyramid[0][0] is the top glass once that glass has been calculated.
	 *
	 * glassPyramid[7][1] is the glass with rowindex 8 and columnindex 2 once that glass has been calculated.
	 *  */
	static Glass[][] glassPyramid = new Glass[MAX_INDEX][MAX_INDEX];

	/**
	 * Main method
	 * @param argv
	 */
	public static void main(String argv[]) {

		double timeToCompleteRace;
		int speed = 12;
		timeToCompleteRace = (1000 / (double)speed);
		System.out.println("speed12:" + timeToCompleteRace);

		speed = 3;
		timeToCompleteRace = (1000 / (double)speed);
		System.out.println("speed3:" + timeToCompleteRace);

		speed = 1000;
		timeToCompleteRace = (1000 / (double)speed);
		System.out.println("speed1000:" + timeToCompleteRace);

		speed = 1001;
		timeToCompleteRace = (1000 / (double)speed);
		System.out.println("speed1001:" + timeToCompleteRace);


		speed = 444444;
		timeToCompleteRace = (1000 / (double)speed);
		System.out.println("speed444444:" + timeToCompleteRace);


		/*
		 * rowIndex starts with 1
		 * columnIndex starts with 1
		 * I.e. when MAX_INDEX is 200, bottom row glass most to the right has index (200,200).
		 * */
		int rowIndex = 10;
		int columnIndex = 5;

		long startTimeMs = System.currentTimeMillis();

		Glass glass = new Glass(rowIndex, columnIndex, null);

		glass.setTimesAndFlows(getInMap(glass));

		// Causes rounding error. Avoid! See https://stackoverflow.com/questions/6860312/any-way-to-avoid-results-containing-9-223372036854776e18
		// double totTime = Math.round(calculateWhenFull(glass) * 1000.00) / 1000.00;
		double totTime = calculateWhenFull(glass);
		System.out.println("It takes " + totTime + " seconds to fill up glass (" + rowIndex + "," + columnIndex + ").");

		long stopTimeMs = System.currentTimeMillis() - startTimeMs;
		System.out.println("Calculation time: " + (stopTimeMs > 1000 ? ((0.001 * stopTimeMs) + " seconds.") : (stopTimeMs + " milliseconds.")));
	}

	/**
	 * Gets the map for a particular glass. The map consists of the different flows (value) in to this glass, and the the start time (key) for each flow.
	 *
	 * Because its a treemap, its sorted by lowest start time first. In other words, keys are getting increasingly bigger.
	 * @param glass
	 * @return TreeMap<Double, Double>
	 *
	 */
	private static TreeMap<Double, Double> getInMap(Glass glass) {

		if (glass.getColumn() < 1 || glass.getColumn() > glass.getRow()) {

			return null;

		} else if (glass.getColumn() == 1 || glass.getColumn() == glass.getRow()) {

			TreeMap<Double, Double> tMap = new TreeMap<>();
			/* 0, 10, 30, 70, 150, 310 etc, formula from Wolfram Alpha: a_n = 5 (2^n - 2)*/
			tMap.put(TIME_TO_FILL_TOP_GLASS_SECONDS * 0.5 * ((Math.pow(2, (glass.getRow()))) - 2) , (1.00/(TIME_TO_FILL_TOP_GLASS_SECONDS * Math.pow(2, (glass.getRow() - 1)))));

			return tMap;

		} else {

			return createInMapFromParents(getLeftParent(glass), getRightParent(glass));

		}

	}

	/**
	 * Get the combined flow to one glass from its parents.
	 * @param leftParent
	 * @param rightParent
	 * @return
	 */
	private static TreeMap<Double, Double> createInMapFromParents(Glass leftParent, Glass rightParent) {

		TreeMap<Double, Double> flowOutFromLeftParent = generateFlowOutToOneChild(leftParent);
		TreeMap<Double, Double> flowOutFromRightParent = generateFlowOutToOneChild(rightParent);

		TreeMap<Double, Double> mapsFromParents = combineInFlowFromTwoMaps(flowOutFromLeftParent, flowOutFromRightParent);

		return mapsFromParents;

	}

	/**
	 * Generate flow map to the child from this glass, based on the startTime.
	 * @param glass
	 * @param startTime
	 * @return
	 */
	private static TreeMap<Double, Double> generateFlowOutToOneChild(Glass glass) {

		TreeMap<Double, Double> returnMap = new TreeMap<Double, Double>();

		double overFlowStartTime = calculateWhenFull(glass);

		for (Map.Entry<Double, Double> entry : glass.getTimesAndFlows().entrySet()) {
			Double key = entry.getKey();
			Double value = entry.getValue() * 0.50;
			if (glass.getTimesAndFlows().entrySet().size() == 1) {
				returnMap.put(overFlowStartTime, value);
			} else if (key < overFlowStartTime) {
				returnMap = new TreeMap<Double, Double>();
				returnMap.put(overFlowStartTime, value);
			} else {
				returnMap.put(key, value);
			}
		}

		return returnMap;

	}

	/**
	 * Combine two maps to their combined map.
	 * @param map1
	 * @param map2
	 * @return
	 */
	private static TreeMap<Double, Double> combineInFlowFromTwoMaps(TreeMap<Double, Double> map1, TreeMap<Double, Double> map2) {

			TreeMap<Double, Double> returnMap = new TreeMap<Double, Double>();

			for (Map.Entry<Double, Double> entry : map1.entrySet()) {
				returnMap.put(entry.getKey(), 0.0);
			}

			for (Map.Entry<Double, Double> entry : map2.entrySet()) {
				returnMap.put(entry.getKey(), 0.0);
			}

			for (Map.Entry<Double, Double> entry : returnMap.entrySet()) {
				Double key = entry.getKey();
				Double valueFrom1 = getValueFromMapFunction(key, map1);
				Double valueFrom2 = getValueFromMapFunction(key, map2);
				Double sum = valueFrom1 + valueFrom2;
				returnMap.put(key, sum);
			}

			return returnMap;

	}

	/**
	 * Helpfunction when combining two maps.
	 * Returns key value from map if it exists. If key doesnt not exist, return the value for previous key and If that doesnt exist return 0.
	 * @param key
	 * @param tMap
	 * @return
	 */
	private static double getValueFromMapFunction(double key, TreeMap<Double, Double> tMap) {

		double ret = 0.00;

		if (tMap.get(key) != null) {
			ret = tMap.get(key);
		} else {
			List<Double> list = new ArrayList<Double>();
			list.addAll(tMap.keySet());
			for (int k = (list.size() - 1); k > -1 ; k--) {
				if (key > list.get(k)) {
					ret = tMap.get(list.get(k));
					break;
				}
			}
		}

		return ret;
	}

	/**
	 *
	 * @param glass
	 * @return
	 */
	private static Glass getLeftParent(Glass glass) {
		/* Get fully populated left parent */
		Glass leftParent = null;
		if (glassPyramid[glass.getRow()-1-1][glass.getColumn()-1-1] != null && glassPyramid[glass.getRow()-1-1][glass.getColumn()-1-1].getTimesAndFlows() != null) {
			/* Use if it already exists. */
			leftParent = glassPyramid[glass.getRow()-1-1][glass.getColumn()-1-1];
		} else {
			leftParent = new Glass((glass.getRow()-1), (glass.getColumn() - 1));
			/* No need to check if mirror glass exist, because then previous if statement would have got it because THAT (the above) glass is set (***). */
			leftParent.setTimesAndFlows(getInMap(leftParent));
		}
		return leftParent;
	}

	/**
	 *
	 * @param glass
	 * @return
	 */
	private static Glass getRightParent(Glass glass) {
		/* Get fully populated right parent */
		Glass rightParent = null;
		if (glassPyramid[glass.getRow()-1-1][glass.getColumn()-1] != null) {
			/* Use if it already exists. */
			rightParent = glassPyramid[glass.getRow()-1-1][glass.getColumn()-1];
		} else {
			rightParent = new Glass((glass.getRow() - 1), (glass.getColumn()));
			/* No need to check if mirror glass exist, because then previous if statement would have got it because THAT (the above) glass is set (***). */
			rightParent.setTimesAndFlows(getInMap(rightParent));
		}
		return rightParent;
	}

	/**
	 * Calculate when a glass is full based on its map.
	 * @param glass
	 * @return
	 */
	private static double calculateWhenFull(Glass glass) {

		TreeMap<Double, Double> tMap = glass.getTimesAndFlows();

		List<Double> keyList = new ArrayList<Double>();
		keyList.addAll(tMap.keySet());

		double waterInGlass = 0.00;
		double result = keyList.get(0);

		for (int k = 0; k < keyList.size(); k++) {
			double rate = tMap.get(keyList.get(k));
			if (k == keyList.size() - 1) {
				/* End of list */
				result += ((1.00 - waterInGlass) / (rate));
			} else {
				double timeIntervall = keyList.get(k + 1) - keyList.get(k);
				double tmpGlassFullness = (waterInGlass) + ((rate) * (timeIntervall));
				if (tmpGlassFullness >= 1.00) {
					/* Glass gets full inside of this time interval.*/
					result += (1.00 - waterInGlass) / (rate);
					break;
				} else {
					result += timeIntervall;
					waterInGlass = tmpGlassFullness;
				}
			}
		}

		populateGlassPyramid(glass);

		return result;
	}

	/**
	 * Populates the glassPyramid with the glass that has been calculated and all done.
	 * @param glass
	 */
	private static void populateGlassPyramid(Glass glass) {
		/* Glasspyramid is a double array that starts with indices 0, and the glass indices starts with index 1, so subtract 1. */
		glassPyramid[glass.getRow() - 1][glass.getColumn() - 1] = glass;
		/* Set the mirror. (***) */
		glassPyramid[glass.getRow() - 1][glass.getRow() - glass.getColumn()] = glass;
	}

}