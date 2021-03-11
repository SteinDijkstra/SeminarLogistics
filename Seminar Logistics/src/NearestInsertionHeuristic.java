import java.util.ArrayList;
import java.util.List;

public class NearestInsertionHeuristic {
	private final static Graph graph = null;
	private final double alpha = 0.75;
	private static List<Location> urgentPlastic = graph.getLocWithContianerOverBound(alpha, 1);
	private static List<Location> urgentGlass = graph.getLocWithContianerOverBound(alpha, 0);
	private static double availableCapacityPlastic = 0; //TODO: Moet nog goed worden bijgehouden
	private static double availableCapacityGlass = 0; //TODO: Moet nog goed worden bijgehouden
	private static double timeUsed;
	private final static double timePlasticRecylcingFacility;
	private final static double timeGlassRecylcingFacility;
	private final static double swapTime = 20;
	
	public static void main(String[] args) {
		
		timeUsed = 0;
		
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
	 * Finds the location with the most predicted garbage of the locations in the given list
	 * @param locations	The list
	 * @param isPlastic
	 * @return
	 */
	public static Location getMaxGarbageLocation(List<Location> locations, boolean isPlastic) {
		if (isPlastic) {
			double maxLevelPlastic = 0;
			Location maxPlasticLocation;
			for(Location i : locations) {
				if (i.getPredictedPlastic() > maxLevelPlastic) {
					maxLevelPlastic = i.getPredictedPlastic();
					maxPlasticLocation = i;
				}	
			}
			return maxPlasticLocation;
		} else {
			double maxLevelGlass = 0;
			Location maxGlassLocation;
			for(Location i : locations) {
				if (i.getPredictedGlass() > maxLevelGlass) {
					maxLevelGlass = i.getPredictedGlass();
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
		return graph.getDistance(leftLocationTour, newLocation) +  graph.getDistance(newLocation, rightLocationTour) - graph.getDistance(leftLocationTour, rightLocationTour) ;
	}
}
