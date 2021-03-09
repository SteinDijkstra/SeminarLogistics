import java.io.IOException;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		testReadTravelTime();
	}
	
	public static void testReadTravelTime() throws NumberFormatException, IOException {
		String filename="travel_time_matrix.csv";
		int[][]result =Utils.readTravelTime(filename);
		for(int[]row:result) {
			for(int element:row) {
				System.out.print(element+",");
			}
			System.out.println("");
		}
	}

}
