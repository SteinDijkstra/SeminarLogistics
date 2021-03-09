import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Basic graph data structure. It has two main variables, the first is the list of locations
 * and the second is a incidence matrix. See Advanced programming for a refresh. This graph will
 * be the main representation of the model.
 * @author steindijkstra
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
		locations= new ArrayList<>();
		this.incidenceMatrix=incidenceMatrix;
		random=new Random(0);
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
	public void addAllLocation(List<Location> newLocations) {
		locations.addAll(newLocations);
	}
	
	/**
	 * Set the seed of the random number generation (used in updating)
	 * @param seed seed to use for random numbers
	 */
	public void setSeet(int seed) {
		random= new Random(seed);
	}
	
	/**
	 * get all locations in the graph
	 * @return
	 */
	public List<Location> getLocations(){
		return locations;
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
	public int getAmountOverflow() {
		int sum =0;
		for(Location loc:locations) {
			if(loc.plasticOverflow()) {sum++;}
			if(loc.glassOverflow()) {sum++;}
		}
		return sum;
	}
	
	
	//--------------Other methods----------------------

}
