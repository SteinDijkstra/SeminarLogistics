import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Maria
 *
 */
public class IntuitiveApproach {
	Graph graph = Utils.init("updated2_travel_time_matrix.csv", "Deposit_data.csv");
	private int nodes = graph.getLocations().size();
	private int timeHorizon = 4;
	private ArrayList<ArrayList<Location>> plasticCubesToVisit = new ArrayList<ArrayList<Location>>();
	private ArrayList<ArrayList<Location>> glassCubesToVisit = new ArrayList<ArrayList<Location>>();

	/**
	 * 
	 */
	public IntuitiveApproach() throws NumberFormatException, IOException {

		
	}

	/**
	 * @param args
	 */
	public void main(String[] args) {
		int totalTime = 0;
		double capacityPlastic = 0;
		double capacityGlass = 0;
		graph.initGarbageMean();
		
		for(int t=0; t<timeHorizon; t++) {
			nextDay();
			ExactSmall.solve(plasticCubesToVisit.get(t), true);
			totalTime = totalTime + ExactSmall.getOptimalTime();
			double capacityPlasticToday = capacityPlastic;
			
			for(int i=0; i<plasticCubesToVisit.get(t).size(); i++) {
				capacityPlastic = capacityPlastic + plasticCubesToVisit.get(t).get(i).emptyPlastic(75);
			}
			
			ExactSmall.solve(glassCubesToVisit.get(t), false);
			totalTime = totalTime + ExactSmall.getOptimalTime();
			double capacityGlassToday = capacityGlass;
			
			for(int i=0; i<glassCubesToVisit.size(); i++) {
				capacityGlass = capacityGlass + glassCubesToVisit.get(t).get(i).emptyGlass(75);
			}
			
			if(capacityPlastic > 75) {
				capacityPlastic = capacityPlastic - capacityPlasticToday;
				totalTime = totalTime + 113;
			}
			
			if(capacityGlass > 75) {
				capacityGlass = capacityGlass - capacityGlassToday;
				totalTime = totalTime + 261;
			}
		}
		
		
		
	}

	public void addPredictedWaste() {
		for(int i=1; i<nodes; i++) {
			graph.getLocation(i).getGlassContainer().changePredictedAmountGarbage(graph.getLocation(i).getGlassContainer().getMeanGarbageDisposed());
			graph.getLocation(i).getPlasticContainer().changePredictedAmountGarbage(graph.getLocation(i).getPlasticContainer().getMeanGarbageDisposed());
		}
	}
	
	public void nextDay() {
		ArrayList<Location> plasticCubesToday = new ArrayList<Location>();
		ArrayList<Location> glassCubesToday = new ArrayList<Location>();
		
		addPredictedWaste();
		
		for(int i=1; i<nodes; i++) {
			
			Container plasticContainer = graph.getLocation(i).getPlasticContainer();
			if(plasticContainer.getPredictedAmountGarbage()>plasticContainer.getCapacity()) {
				plasticCubesToday.add(graph.getLocation(i));
			}
			
			Container glassContainer = graph.getLocation(i).getGlassContainer();
			if(glassContainer.getPredictedAmountGarbage()>glassContainer.getCapacity()) {
				glassCubesToday.add(graph.getLocation(i));
			}
			
		}
		
		plasticCubesToVisit.add(plasticCubesToday);
		glassCubesToVisit.add(glassCubesToday);
	}
}
