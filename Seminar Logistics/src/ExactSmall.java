import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact method to solve the Vehicle Routing Problem by enumerating all possible routes
 * and choosing the cheapest. This class first decides which containers must be visited 
 * and then enumerates all possible routes recursively, for the sake of efficiency.
 * @author Stein
 */
public class ExactSmall {
	private static Graph model; // TODO initialize right graph
	private static int optimalTime = Integer.MAX_VALUE;
	private static List<Integer> optimalRoute;

	public static void main(String[] args) throws NumberFormatException, IOException {
		model = Utils.init();
		model.initGarbage();
		model.updateGarbage();

		// First check which containers should be visited today.
		List<Location> toVisit = model.getLocWithContainerOverBound(1, true);
		// TODO: check hoeveel containers dit zijn, voordat we verder gaan.
		System.out.println("find optimal route through: "+ toVisit);
		solve(toVisit, true);
		// The other printed line can be outcommented in the findRoute method.
		System.out.println("Optimal route: " + getOptimalRoute() + " with time: " + getOptimalTime());
	}


	/** This method solves the model exactly, given the list of locations that must be visited
	 * @param toVisit a list of locations of which we will form a route
	 * @param isPlastic boolean plastic or not; necessary for picking right containers on location.
	 */
	public static void solve(List<Location> toVisit, boolean isPlastic) {
		List<Integer> currentRoute = new ArrayList<>();
		// We start at the depot
		currentRoute.add(0);
		int emptyTime = 0;
		/* For each location visited, we add the time that we are busy emptying,
		 * which is independent of the route we take. However, it affects the total
		 * time spent and is thus important for the time constraint.
		 */
		for(Location loc : toVisit) {
			if(isPlastic) {
				emptyTime += loc.getPlasticEmptyTime();
			} else {
				emptyTime += loc.getGlassEmptyTime();
			}
		}
		optimalRoute = null;
		optimalTime = Integer.MAX_VALUE;
		findRoute(toVisit, currentRoute, emptyTime);
		/* Check if we should reverse the route by seeing whether the first or last container that we're visiting
		 * is more likely to overflow in the near future. We want the emptiest of the two at the end, because it
		 * is a possibility that we can only empty it partially (or not at all). 
		 */
		if(isPlastic) {
			if(model.getLocation(1).getPredictedPlastic() < model.getLocation(optimalRoute.size() - 2).getPredictedPlastic()) {
				Collections.reverse(optimalRoute);
			}
		}
		else {
			if(model.getLocation(1).getPredictedGlass() < model.getLocation(optimalRoute.size() - 2).getPredictedGlass()) {
				Collections.reverse(optimalRoute);
			}
		}
		System.out.println("New solution: " + optimalRoute + " with time: " + optimalTime);
	}

	/** This method enumerates all routes recursively, keeping track of the best route and time,
	 * saving those in variables optimalTime and optimalRoute.
	 * The lists are dynamic, where the list of locations to visit gets emptier over time and a 
	 * route can only be formed if this list is empty, so that all locations are present in the route.
	 * @param toVisit list of locations that we still need to add to the route.
	 * @param currentRoute the list of locations in order of the current route.
	 * @param currentTime keeping track of the current time desired by this route.
	 */
	public static void findRoute(List<Location>toVisit, List<Integer>currentRoute, int currentTime) {
		// Base case; if list toVisit is empty, a full route was constructed
		if(toVisit.isEmpty()) {
			int lastDistance = model.getDistance(currentRoute.get(currentRoute.size() - 1), 0);
			currentTime += lastDistance;
			currentRoute.add(0);
			// System.out.println("New solution: " + currentRoute + " with time: " + currentTime);
			if(currentTime < optimalTime) {
				optimalTime = currentTime;
				optimalRoute = new ArrayList<>(currentRoute);

			}
			// Subtract and remove the last part for backtracking purposes.
			currentTime -= lastDistance;
			currentRoute.remove(currentRoute.size() - 1);
		} else {
			// Add all locations one by one and call method recursively to obtain full route
			for(int i = 0; i < toVisit.size(); i++) {
				Location newLoc = toVisit.remove(i);
				int dist = model.getDistance(currentRoute.get(currentRoute.size() - 1), newLoc.getIndex());
				currentTime += dist;
				currentRoute.add(newLoc.getIndex());
				findRoute(toVisit, currentRoute, currentTime);
				// Backtracking purposes
				currentTime -= dist;
				toVisit.add(i, newLoc);
				currentRoute.remove(currentRoute.size() - 1);
			}
		}
	}

	/** This method returns the optimal route obtained by solve method.
	 * WARNING: DO NOT CALL THIS METHOD BEFORE CALLING SOLVE.
	 * @return a list of location numbers that 
	 */
	public static List<Integer> getOptimalRoute(){
		return optimalRoute;
	}

	/** This method returns the optimal time belonging to the optimal route.
	 * WARNING: DO NOT CALL THIS METHOD BEFORE CALLING SOLVE.
	 * @return integer value of optimal time
	 */
	public static int getOptimalTime() {
		return optimalTime;
	}
	
	/**
	 * This method sets the model for the exact small instance.
	 * @param model the graph
	 */
	public static void setModel(Graph model) {
		ExactSmall.model = model;
	}
	
	/**
	 * This method returns the graph of the model.
	 * @return the model graph
	 */
	public static Graph getModel() {
		return ExactSmall.model;
	}
}