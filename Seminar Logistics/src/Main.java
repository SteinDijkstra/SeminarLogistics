import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model = Utils.init();
		model.initGarbage();
		model.updateGarbage();
		
		List<Location> predictedOverflowList = model.getLocWithContainerOverBound(1, true);
		Utils.printLocations(predictedOverflowList);
	}
}