import java.util.ArrayList;
import java.util.List;

/** This class enumerates all routes given a small instance of locations and then
 * chooses the route which takes least amount of time
 * @author Marja
 * 
 */
public class ExactSmallInstanceSolution {
	private final static Graph graph = null; // TODO initialize right graph
	private static List<Location> bestRoute = null;
	public static void main(String[] args) {
		List<Location> allLocations = graph.getLocationsExceptDepot();
		List<List<Location>> allRoutes = null;
		allRoutes = enumerateRoutes(allLocations, 0, allRoutes);
		List<Integer> routeLengths = findRouteLengths(allRoutes);
		int solution = findMinimum(routeLengths);
		int routeNumber = routeLengths.indexOf(solution);
		List<Location> bestRoute = allRoutes.get(routeNumber);
		printRoute(bestRoute);
	}
	
	/**
	 * This recursive method will generate all permutations of a String, split by character.
	 * @param locationOrder is an array of characters, containing the separate letters of the word.
	 * @param currentIndex is the current index on which we are still swapping.
	 * @param allRoutes is a list which contains all permutations until now.
	 * @return a list of all permutations of the inserted array with letters.
	 */
	public static List<List<Location>> enumerateRoutes(List<Location> locationOrder, int currentIndex, List<List<Location>> allRoutes) {
		// If the traversing of all locations is done, add the current route to the list.
		if (currentIndex == locationOrder.size() - 1) {
			allRoutes.add(locationOrder);
		}	
		for (int i = currentIndex; i < locationOrder.size(); i++) {
			// Swap two locations
			swap(locationOrder, currentIndex, i);
			// Recursion by recalling the method but now with next index.
			enumerateRoutes(locationOrder, currentIndex + 1, allRoutes);
			// Also at the end swap the last two locations.
			swap(locationOrder, currentIndex, i);
		}
		return allRoutes;
	}

	/** Utility function to swap two locations in a route list.
	 * @param routeList the list in which two locations should be swapped.
	 * @param i the index of the first location to be swapped.
	 * @param j the index of the second location to be swapped.
	 */
	public static void swap(List<Location> routeList, int i, int j) {
		Location temper = routeList.get(i);
		routeList.set(i, routeList.get(j));
		routeList.set(j, temper);
	}
	
	/** Method that calculates all lengths for a given list of routes
	 * @param routes a list of lists of locations in certain orders
	 * @return a list of all lengths of the routes.
	 */
	public static List<Integer> findRouteLengths(List<List<Location>> routes){
		Location deposit = graph.getLocation(0);
		List<Integer> routeLengths = new ArrayList<>();
		int currentLength = 0;
		for(List<Location> route : routes) {
			// First add length of deposit to first node in the route
			currentLength += graph.getDistance(deposit, route.get(0));
			// Then add all lengths from nodes to nodes in the route
			for(int i = 1; i < route.size(); i++) {
				currentLength += graph.getDistance(route.get(i-1), route.get(i));
			}
			// Finally add the length from last node to deposit
			currentLength += graph.getDistance(route.get(route.size() - 1), deposit);
			routeLengths.add(currentLength);
		}
		return routeLengths;
	}
	
	/** Util method that prints the node numbers used in the route
	 * @param route that should be printed
	 */
	public static void printRoute(List<Location> route) {
		System.out.print("The used route is: 0, ");
		Utils.printLocations(route);
		System.out.println("0.");
	}
	
	/** Utility method that finds the minimum number in a list of numbers
	 * @param input a list of numbers
	 * @return the minimum value in the list
	 */
	public static int findMinimum(List<Integer> input) {
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The list is empty");
		}
		int solution = Integer.MAX_VALUE;
		for(int i : input) {
			if(i < solution) {
				solution = i;
			}
		}
		return solution;
	}
}
