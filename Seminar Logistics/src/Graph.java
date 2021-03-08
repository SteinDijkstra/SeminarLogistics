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
		random =new Random(0);
	}
	/**
	 * Create an empty graph with specified distances; Locations should be added in later
	 * @param incidenceMatrix int matrix with distances 
	 * @param seed specified seed for the random numbers
	 */
	public Graph(int[][] incidenceMatrix, int seed) {
		locations= new ArrayList<>();
		this.incidenceMatrix= incidenceMatrix;
		random =new Random(seed);
	}
	
	//--------------Setters and getters----------------
	/**
	 * 
	 * @param newLocation
	 */
	public void addLocation(Location newLocation) {
		locations.add(newLocation);
	}
	
	public List<Location> getLocations(){
		return locations;
	}
	
	public int getDistance(int from, int to) {
		return incidenceMatrix[from][to];
	}
	
	
	//--------------Utility methods--------------------
	
	
	
	//--------------Other methods----------------------

}
