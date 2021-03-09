import java.io.IOException;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model= Utils.init();
		System.out.println(model.getDistance(1, 2));
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
	
	public static boolean testTriangleInequality(int[][]travelTimes) {
		for(int i=0;i<travelTimes.length;i++) {
			for(int j=0;j<travelTimes.length;j++) {
				for(int k=0;k<travelTimes.length;k++) {
					if(travelTimes[i][j]>travelTimes[i][k]+travelTimes[k][j]) {
						System.out.println("from "+i+" to "+j+" takes "+ travelTimes[i][j]+" minutes");
						System.out.println("from "+i+" to "+j+" via "+k+" takes "+ travelTimes[i][k]+" + "+travelTimes[k][j]+" minutes");
					}
				}
			}
		}
		return true;
	}

}
