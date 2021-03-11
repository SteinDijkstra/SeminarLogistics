import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model= Utils.init();
		model.initGarbage();
		model.updateGarbage();
		List<Location> predictedOverflowList= model.getLocWithContianerOverBound(1, true);
		for(Location loc:predictedOverflowList) {
			System.out.println(loc);
		}
		
	
	}
	


}
