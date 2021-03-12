import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model = Utils.init();
		model.initGarbage();
		List<Location> predictedOverflowList = model.getLocWithContainerOverBound(1, true);
		//TODO: call printing method from another class
		Utils.printLocations(predictedOverflowList);
	}
}