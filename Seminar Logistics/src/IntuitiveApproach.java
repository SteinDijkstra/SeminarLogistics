import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Marian, Manuela
 *
 */
public class IntuitiveApproach {
	Graph graph = Utils.init("updated2_travel_time_matrix.csv", "Deposit_data.csv");
	private int nodes = graph.getLocations().size();
	private int timeHorizon = 4;
	private ArrayList<ArrayList<Location>> plasticCubesToVisit = new ArrayList<ArrayList<Location>>();
	private ArrayList<ArrayList<Location>> glassCubesToVisit = new ArrayList<ArrayList<Location>>();
	private boolean isPlastic = true; // initialize as plastic container on truck TODO: maybe randomize this?

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
		double plasticCollected = 0;
		double glassCollected = 0;
		graph.initGarbageMean();
		
		for(int t=0; t<timeHorizon; t++) {
			nextDay(); // get list of locations to empty next day
			ExactSmall.solve(plasticCubesToVisit.get(t), true); // get route of plastic locations to empty
			totalTime = totalTime + ExactSmall.getOptimalTime(); // update total time spent
			boolean emptyPlastic = !ExactSmall.getOptimalRoute().isEmpty(); // boolean for emptying plastic cubes or not
			double capacityPlasticToday = plasticCollected;
			// update collected amount of plastic
			for(int i=0; i<plasticCubesToVisit.get(t).size(); i++) {
				plasticCollected = plasticCollected + plasticCubesToVisit.get(t).get(i).emptyPlastic(75-plasticCollected);
			}
			ExactSmall.solve(glassCubesToVisit.get(t), false); // get route of glass locations to empty
			totalTime = totalTime + ExactSmall.getOptimalTime(); // update total time spent
			boolean emptyGlass = !ExactSmall.getOptimalRoute().isEmpty(); // boolean for emptying glass cubes or not
			double capacityGlassToday = glassCollected;
			// update collected amount of glass
			for(int i=0; i<glassCubesToVisit.size(); i++) {
				glassCollected = glassCollected + glassCubesToVisit.get(t).get(i).emptyGlass(75-glassCollected);
			}
			// go to recycling facility if necessary, including swaps
			boolean toRecycling = false;
			if (plasticCollected >= 75 && glassCollected >= 75 && isPlastic) { // go to plastic recycling, no swap needed
				toRecycling = true;
				plasticCollected = 0;
				totalTime = totalTime + 113;
			}
			else if (plasticCollected >= 75 && glassCollected >= 75 && !isPlastic && !toRecycling) { // go to glass recycling, no swap needed
				toRecycling = true;
				glassCollected = 0;
				totalTime = totalTime + 261;
			}
			else if (plasticCollected >= 75 && glassCollected < 75 && !isPlastic && !toRecycling) { // swap needed to go to recycling facility
				isPlastic = true;
				toRecycling = true;
				plasticCollected = 0;
				totalTime = totalTime + 133;
			}
			else if (glassCollected >= 75 && plasticCollected < 75 && isPlastic && !toRecycling) { // swap needed to go to recycling facility
				isPlastic = false;
				toRecycling = true;
				glassCollected = 0;
				totalTime = totalTime + 281;
			}
			// swap after recycling facility
			if (emptyPlastic && emptyGlass && isPlastic) { // first empty plastic, swap, empty glass
				totalTime = totalTime + 20;
				isPlastic = false;
			}
			else if (emptyPlastic && emptyGlass && !isPlastic) { // first empty glass, swap, empty plastic
				totalTime = totalTime + 20;
				isPlastic = true;
			}
			else if (!emptyPlastic && emptyGlass && isPlastic) { // swap to glass container before emptying
				totalTime = totalTime + 20;
				isPlastic = false;
			}
			else if (emptyPlastic && !emptyGlass && !isPlastic) { // swap to plastic container before emptying
				totalTime = totalTime + 20;
				isPlastic = true;
			}
			// TODO: hoe te bepalen met welke container (plastic/glass) zijn dag moet eindigen?
			
			// ?
			if(plasticCollected >= 75) {
				plasticCollected = plasticCollected - capacityPlasticToday;
				totalTime = totalTime + 113;
			}
			
			if(glassCollected >= 75) {
				glassCollected = glassCollected - capacityGlassToday;
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
			if(plasticContainer.getPredictedAmountGarbage()>plasticContainer.getCapacity()) { // TODO: betekent dat we predicted gebruiken dat dit stochastisch model is?
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
