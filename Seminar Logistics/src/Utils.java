import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stein
 *
 */
public class Utils {
	public static Graph init() throws NumberFormatException, IOException {
		return init("travel_time_matrix.csv","Deposit_data.csv");
	}

	public static Graph initSubgraph(String travelTime, String depositData, List<Integer> locationNumbers) throws NumberFormatException, IOException {
		// First construct a smaller distance matrix
		// Deposit must always be included, otherwise throw exception.
		if(!locationNumbers.contains(0)) {
			throw new IllegalArgumentException("Please always add the deposit, number 0");
		}
		// Two indices i and j keep track of where we are, filling the distance matrix.
		int i = 0; //row index
		int j = 0; //column index
		int size = locationNumbers.size();
		int[][] fullDistances = readTravelTime(travelTime);
		int[][] distances = new int[size + 1][size + 1];
		//TODO: check if matrix is filled in right way
		for(int k : locationNumbers) {
			for(int l : locationNumbers) {
				distances[i][j] = fullDistances[k][l];
				j++;
			}
			i++;
		}
		Graph graph = new Graph(distances);
		
		// Secondly, add right locations to the location list
		List<Location> fullLocations = readLocations(depositData, graph);
		for(Location l : fullLocations) {
			if(locationNumbers.contains(l.getIndex())) {
				graph.addLocation(l);
			}
		}
		return graph;
	}

	public static Graph init(String travelTime, String depositData) throws NumberFormatException, IOException {
		int[][]distances = readTravelTime(travelTime);
		Graph graph = new Graph(distances);
		graph.addAllLocations(readLocations(depositData,graph));
		return graph;
	}

	public static int[][] readTravelTime(String travelTime) throws NumberFormatException, IOException {
		try(BufferedReader scan = new BufferedReader(new FileReader(new File(travelTime) ))){
			int nNodes = Integer.parseInt(scan.readLine());
			int[][] result = new int[nNodes][nNodes];

			String newLine;
			int from = 0;
			while((newLine = scan.readLine())!=null) {
				String[] asciiNumbers=newLine.split(";");
				for(int to = 0; to < asciiNumbers.length; to++) {
					result[from][to] = Integer.parseInt(asciiNumbers[to]); 
				}
				from++;
			}
			return result;
		}
	}

	/** This method reads all locations from the data files
	 * @param depositData file that is read
	 * @param graph ADT we are putting the data in
	 * @return a list of all locations read, INCLUDING DEPOSIT
	 * @throws NumberFormatException throws if input doesn't only consist of numbers
	 * @throws IOException throws if reading file goes wrong
	 */
	public static List<Location> readLocations(String depositData, Graph graph) throws NumberFormatException, IOException {
		try(BufferedReader scan = new BufferedReader(new FileReader(new File(depositData) ))){
			List<Location> result = new ArrayList<>();

			String newLine = scan.readLine();
			// first set newLine equal to the next line and then test if it's null
			while((newLine = scan.readLine()) != null) {
				String[] splitData = newLine.split(";");
				Container glass = new Container(Double.parseDouble(splitData[1]), Double.parseDouble(splitData[3]), Double.parseDouble(splitData[4]));
				Container plastic = new Container(Double.parseDouble(splitData[5]), Double.parseDouble(splitData[7]), Double.parseDouble(splitData[8]));
				Location newLocation = new Location(graph, Integer.parseInt(splitData[0]), glass, plastic, Integer.parseInt(splitData[2]), Integer.parseInt(splitData[6]));
				result.add(newLocation);
			}
			return result;
		}
	}

	/** Util method prints all indices of locations
	 * @param locations whose indices should be printed
	 */
	public static void printLocations(List<Location> locations) {
		for(Location loc : locations) {
			System.out.print(loc.getIndex() + ", ");
		}
	}
}
