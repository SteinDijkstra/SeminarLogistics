import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stein
 *
 */
public class Utils {
	public static Graph init() throws NumberFormatException, IOException {
		return init("updated2_travel_time_matrix.csv","Deposit_data.csv");
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
	
	public static int[][] readDeposits(String deposits) throws NumberFormatException, IOException {
		try(BufferedReader scan = new BufferedReader(new FileReader(new File(deposits) ))){
			int nNodes = Integer.parseInt(scan.readLine());
			int[][] result = new int[nNodes][200];

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

	public static void solveTriangleInequality(String travelTime,String outputFile) throws NumberFormatException, IOException {
		int[][]distances=readTravelTime(travelTime);
		int[][]newDistances=new int[distances.length][distances.length];
		int minDistance;
		for(int i = 0; i < distances.length; i++) {
			for(int j = 0; j < distances.length; j++) {
				minDistance=distances[i][j];
				for(int k = 0; k < distances.length; k++) {
					if(minDistance > distances[i][k] + distances[k][j]) {
						minDistance=distances[i][k] + distances[k][j];
					}
				}
				newDistances[i][j]=minDistance;
			}
		}
		writeFile(newDistances,outputFile);
	}

	public static void writeFile(int[][] matrix,String filename) throws IOException {
		try(BufferedWriter br=new BufferedWriter(new FileWriter(new File(filename)))){
			br.write(""+matrix.length);
			br.newLine();
			for(int[] row:matrix) {
				br.write(""+row[0]);
				for(int i=1;i<row.length;i++) {
					br.write(";"+row[i]);
				}
				br.newLine();
			}
		}
	}
}
