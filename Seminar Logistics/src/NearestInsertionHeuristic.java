import java.util.ArrayList;
import java.util.List;

/**
 * @author Marian
 *
 */
public class NearestInsertionHeuristic {
	private final static Graph graph = null; //TODO:initizalize right graph
	private final static double alpha = 0.75;
	private static List<Location> urgentPlastic = graph.getLocWithContainerOverBound(alpha, true);
	private static List<Location> urgentGlass = graph.getLocWithContainerOverBound(alpha, false);
	private static double availableCapacityPlastic = 0; //TODO: Moet nog goed worden bijgehouden
	private static double availableCapacityGlass = 0; //TODO: Moet nog goed worden bijgehouden
	private static double timeUsed;
	private final static double timePlasticRecylcingFacility = 113;
	private final static double timeGlassRecylcingFacility = 261;
	private final static double swapTime = 20;

	public static void main(String[] args) {

		timeUsed = 0;

		//Calculate whether the containers can facilitate the wast which should be collected
		double neededCapacityPlastic = 0;
		double neededCapacityGlass = 0;

		for(Location i:urgentPlastic) {
			neededCapacityPlastic =+ i.getPredictedPlastic();
		}

		for(Location i:urgentGlass) {
			neededCapacityGlass =+ i.getPredictedGlass();
		}

		//TODO: moet er nog een margin bij?
		if(availableCapacityPlastic < neededCapacityPlastic) {
			timeUsed =+ timePlasticRecylcingFacility;
		}

		//TODO: moet er nog een margin bij?
		if(availableCapacityGlass < neededCapacityGlass) {
			if(timeUsed != 0) {
				timeUsed =+ swapTime;
			}
			timeUsed =+ timeGlassRecylcingFacility;
		}

		//Adding the first location to a route
		Location firstPlasticNode = null;
		Location firstGlassNode = null;
		List<Location> routePlastic = new ArrayList<>();
		List<Location> routeGlass = new ArrayList<>();

		if(!urgentPlastic.isEmpty()) {
			firstPlasticNode = getMaxGarbageLocation(urgentPlastic, true);
			timeUsed =+ 2*graph.getDistance(0, firstPlasticNode.getIndex());
			if(!urgentGlass.isEmpty()) {
				timeUsed =+ swapTime;
			}
		}
		if(!urgentGlass.isEmpty()) {
			firstGlassNode = getMaxGarbageLocation(urgentGlass, true);
			timeUsed =+ 2*graph.getDistance(0, firstGlassNode.getIndex());
		}		
	}


	/**
	 * Finds the location with the most predicted garbage percentage of the locations in the given list
	 * @param locations	The list with locations there will be searched in
	 * @param isPlastic	Whether or not there should be searched between the plastic cubes
	 * @return			The location with the relative most filled cube(s)
	 */
	public static Location getMaxGarbageLocation(List<Location> locations, boolean isPlastic) {
		if (isPlastic) {
			double maxLevelPlastic = 0;
			Location maxPlasticLocation = null;

			for(Location i : locations) {
				double newLevelPlastic = i.getPredictedPlastic()/i.getPlasticContainer().getCapacity();
				if (newLevelPlastic > maxLevelPlastic) {
					maxLevelPlastic = newLevelPlastic;
					maxPlasticLocation = i;
				}	
			}

			return maxPlasticLocation;

		} else {
			double maxLevelGlass = 0;
			Location maxGlassLocation = null;

			for(Location i : locations) {
				double newLevelGlass = i.getPredictedGlass()/i.getGlassContainer().getCapacity();
				if (newLevelGlass > maxLevelGlass) {
					maxLevelGlass = newLevelGlass;
					maxGlassLocation = i;
				}	
			}
			return maxGlassLocation;
		}
	}


	/**
	 * Calculates the time which is needed to insert the new location between the 
	 * left location and the right location.
	 * @param leftLocationTour 	the location which should be visited before the new location
	 * @param rightLocationTour	the location which should be visited after the new location
	 * @param newLocation		the location which should be inserted
	 * @return					the extra time this insertion takes
	 */
	public static int getExtraTime(int leftLocationTour, int rightLocationTour, int newLocation) {
		return graph.getDistance(leftLocationTour, newLocation) +  graph.getDistance(newLocation, rightLocationTour) 
		- graph.getDistance(leftLocationTour, rightLocationTour) ;
	}
}
