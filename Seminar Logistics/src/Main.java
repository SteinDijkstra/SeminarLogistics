import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		Test.testTriangleInequality(Utils.readTravelTime("travel_time_matrix.csv"));
		//Test.testTriangleInequality(Utils.readTravelTime("updated_travel_time_matrix.csv"));
		//Test.testTriangleInequality(Utils.readTravelTime("updated2_travel_time_matrix.csv"));
		//Utils.solveTriangleInequality("updated_travel_time_matrix.csv", "updated2_travel_time_matrix.csv");
		
		
	}
}