import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Basic graph data structure. It has two main variables, the first is the list of locations
 * and the second is a incidence matrix. See Advanced programming for a refresh. This graph will
 * be the main representation of the model.
 * @author Stein
 *
 */
public class Graph {
	private List<Location> locations;//list of locations index also is the location Number
	private int[][] incidenceMatrix;//incidence matrix, element i,j is equal to the travel time from i to j
	private Random random;
	//-------------------Constructors-----------------
	/**
	 * Create an empty graph with specified distances; Locations should be added in later
	 * @param incidenceMatrix int matrix with distances
	 */
	public Graph(int[][] incidenceMatrix) {
		locations = new ArrayList<>();
		this.incidenceMatrix = incidenceMatrix;
		random = new Random(0);
	}

	//--------------Setters and getters----------------
	/**
	 * add new location to the model ONLY use for initialization
	 * @param newLocation a single location to add
	 */
	public void addLocation(Location newLocation) {
		locations.add(newLocation);
	}
	/**
	 * add a list of locations to the model ONLY use for initialization
	 * @param newLocation a list of locations corresponding to the distance matrix
	 */
	public void addAllLocations(List<Location> newLocations) {
		locations.addAll(newLocations);
	}

	public void removeLocation(Location remLocation) {
		if(!locations.contains(remLocation)) {
			throw new IllegalArgumentException("Please input location that exists in this list");
		}
		locations.remove(remLocation);
	}

	/**
	 * Set the seed of the random number generation (used in updating)
	 * @param seed seed to use for random numbers
	 */
	public void setSeed(int seed) {
		random= new Random(seed);
	}

	/**
	 * get all locations in the graph, including deposit
	 * @return list of all locations in graph
	 */
	public List<Location> getLocations(){
		return locations;
	}

	/** get all locations in the graph, excluding deposit
	 * @return list of all locations, but not deposit
	 */
	public List<Location> getLocationsExceptDeposit() {
		List<Location> temp = getLocations();
		temp.remove(0);
		return temp;
	}

	/**
	 * Returns the Location at the specified index
	 * @param index integer of the location to access
	 * @return a Location
	 */
	public Location getLocation(int index) {
		return locations.get(index);
	}

	/**
	 * Get distance from a location to another location
	 * @param from index of original location
	 * @param to index of next location
	 * @return integer distance between them
	 */
	public int getDistance(int from, int to) {
		return incidenceMatrix[from][to];
	}

	/** Get distance from a location to another location
	 * @param from original location
	 * @param to next location
	 * @return integer distance between them
	 */
	public int getDistance(Location from, Location to) {
		int fromIndex = from.getIndex();
		int toIndex = to.getIndex();
		return getDistance(fromIndex, toIndex);
	}

	/**
	 * Get the distances to all neighbors from a certain node
	 * @param from original node
	 * @return integer array with distances to other nodes.
	 */
	public int[] getDistanceNeighbours(int from) {
		return incidenceMatrix[from];
	}

	/**
	 * Return the number of locations in the graph
	 * @return a integer amount
	 */
	public int getNumLocations() {
		return locations.size();
	}

	//--------------Utility methods--------------------
	/**
	 * Counts the total number of overflow locations
	 * @return the total number of overflow locations
	 */
	public int getAmountOverflow() {
		int sum = 0;
		for(Location loc:locations) {
			if(loc.plasticOverflow()) {
				sum++;
			}
			if(loc.glassOverflow()) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * Initializes the garbage bins
	 */
	public void initGarbage() {
		for(Location loc:locations) {
			//Set plastic value
			Container plastic = loc.getPlasticContainer();
			double amountPlastic = plastic.getCapacity() * random.nextDouble();
			plastic.setActualAmountGarbage(amountPlastic);
			plastic.setPredictedAmountGarbage(amountPlastic);//TODO exact amount not known.

			//Set glass values
			Container glass = loc.getGlassContainer();
			double  amountGlass = glass.getCapacity() * random.nextDouble();
			glass.setActualAmountGarbage(amountGlass);
			glass.setPredictedAmountGarbage(amountGlass);
		}
	}

	/**
	 * update the garbage at the end of the day 
	 */
	public void updateGarbage() {
		for(Location loc:locations) {
			//update plastic value
			Container plastic = loc.getPlasticContainer();
			double randomNormal = random.nextGaussian();
			plastic.update(randomNormal);

			//Set glass values
			Container glass = loc.getGlassContainer();
			randomNormal = random.nextGaussian();
			glass.update(randomNormal);
		}
	}
	//--------------Solution/heuristic methods----------------------
	/**
	 * Returns a list of locations where the predicted garbage of container of type isPlastic 
	 * is larger than percentage times the max capacity
	 * @param percentage percentage to set the bound to number between 0 and 1
	 * @param isPlastic true if plastic 
	 * @return
	 */
	public List<Location> getLocWithContainerOverBound(double percentage, boolean isPlastic){
		List<Location> result= new ArrayList<>();
		for(Location loc:locations) {
			Container container;
			if(isPlastic) {
				container = loc.getPlasticContainer();
			} else {
				container = loc.getGlassContainer();
			}
			if(container.getPredictedAmountGarbage() > percentage * container.getCapacity()) {
				result.add(loc);
			}
		}
		return result;
	}
}