import java.io.IOException;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model= Utils.init();
		System.out.println(model.getDistance(1, 2));
		Test.testReadTravelTime();
	}
	


}
