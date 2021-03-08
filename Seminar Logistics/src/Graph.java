import java.util.List;

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
	
	public Graph() {
		
	}

}
