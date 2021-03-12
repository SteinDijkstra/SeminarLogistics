import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static Graph init() throws NumberFormatException, IOException {
		return init("travel_time_matrix.csv","Deposit_data.csv");
	}
	public static Graph initSmall() {
		System.out.println("not yet implemented");
		return null;
	}
	
	public static Graph init(String travelTime, String depositData) throws NumberFormatException, IOException {
		int[][]distances=readTravelTime(travelTime);
		Graph graph= new Graph(distances);
		graph.addAllLocation(readLocations(depositData,graph));
		return graph;
	}
	
	public static int[][] readTravelTime(String travelTime) throws NumberFormatException, IOException {
		try(BufferedReader scan=new BufferedReader(new FileReader(new File(travelTime) ))){
			int nNodes= Integer.parseInt(scan.readLine());
			int[][] result = new int[nNodes][nNodes];
			
			String newLine;
			int from=0;
			while((newLine=scan.readLine())!=null) {
				String[] asciiNumbers=newLine.split(";");
				for(int to=0;to<asciiNumbers.length;to++) {
					result[from][to]=Integer.parseInt(asciiNumbers[to]); 
				}
				from++;
			}
			return result;
		}
	}
	
	public static List<Location> readLocations(String depositData, Graph graph) throws NumberFormatException, IOException {
		try(BufferedReader scan=new BufferedReader(new FileReader(new File(depositData) ))){
			
			List<Location> result = new ArrayList<>();
			
			String newLine=scan.readLine();
			while((newLine=scan.readLine())!=null) {
				String[] splitData=newLine.split(";");
				Container glass= new Container(Double.parseDouble(splitData[1]), Double.parseDouble(splitData[3]), Double.parseDouble(splitData[4]));
				Container plastic= new Container(Double.parseDouble(splitData[5]), Double.parseDouble(splitData[7]), Double.parseDouble(splitData[8]));
				Location newLocation= new Location(graph, Integer.parseInt(splitData[0]), glass, plastic, Integer.parseInt(splitData[2]), Integer.parseInt(splitData[6]));
				result.add(newLocation);
			}
			return result;
		}
	}
}
