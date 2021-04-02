import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Marian, Manuela
 *
 */
public class IntuitiveApproach {
	private static Graph graph;
	private static int nodes;
	private static int timeHorizon;
	private static ArrayList<ArrayList<Location>> plasticCubesToVisit;
	private static ArrayList<ArrayList<Location>> glassCubesToVisit;
	private static int[] totalTime;
	private static double[] plasticCollected;
	private static double[] glassCollected;
	private static boolean[] toPlasticRecycling;
	private static boolean[] toGlassRecycling;
	private static boolean[] isPlastic;

	public static void main(String[] args) throws NumberFormatException, IOException {
		graph = Utils.init("updated2_travel_time_matrix.csv", "Deposit_data.csv");
		ExactSmall.setModel(graph);
		nodes = graph.getLocations().size();
		timeHorizon = 200;
		plasticCubesToVisit = new ArrayList<ArrayList<Location>>();
		glassCubesToVisit = new ArrayList<ArrayList<Location>>();
		totalTime = new int[timeHorizon];
		plasticCollected = new double[timeHorizon];
		glassCollected = new double[timeHorizon];
		toPlasticRecycling = new boolean[timeHorizon];
		toGlassRecycling = new boolean[timeHorizon];
		isPlastic = new boolean[timeHorizon];
		isPlastic[0] = true;
		solve();
		output();
	}

	/**
	 * @param args
	 */
	public static void solve() {
		graph.initGarbageMean();
		
		for(int t=0; t<timeHorizon; t++) {
			totalTime[t] = 0;
			plasticCollected[t] = 0;
			glassCollected[t] = 0;
			toPlasticRecycling[t] = false;
			toGlassRecycling[t] = false;
			boolean emptyPlastic = false;
			boolean emptyGlass = false;
			if (t > 0) 
				isPlastic[t] = isPlastic[t-1];
			nextDay(); // get list of locations to empty next day
			if(!plasticCubesToVisit.get(t).isEmpty()) {
				ExactSmall.solve(plasticCubesToVisit.get(t), true); // get route of plastic locations to empty
				totalTime[t] = totalTime[t] + ExactSmall.getOptimalTime(); // update total time spent
				emptyPlastic = true; // boolean for emptying plastic cubes or not
			}
			// update collected amount of plastic
			for(int i=0; i < plasticCubesToVisit.get(t).size(); i++) {
				plasticCollected[t] = plasticCollected[t] + plasticCubesToVisit.get(t).get(i).emptyPlastic(75-plasticCollected[t]);
			}
			if (t > 0)
				plasticCollected[t] = plasticCollected[t] + plasticCollected[t-1];
			if(!glassCubesToVisit.get(t).isEmpty()) {
				ExactSmall.solve(glassCubesToVisit.get(t), false); // get route of glass locations to empty
				totalTime[t] = totalTime[t] + ExactSmall.getOptimalTime(); // update total time spent
				emptyGlass = true; // boolean for emptying glass cubes or not
			}
			// update collected amount of glass
			for(int i=0; i < glassCubesToVisit.get(t).size(); i++) {
				glassCollected[t] = glassCollected[t] + glassCubesToVisit.get(t).get(i).emptyGlass(75-glassCollected[t]);
			}
			if (t > 0)
				glassCollected[t] = glassCollected[t] + glassCollected[t-1];
			// go to recycling facility if necessary, including swaps
			if (plasticCollected[t] >= 75 && glassCollected[t] >= 75 && isPlastic[t]) { // go to both recycling facilities, check on which day which container to empty, kijk naar welke dag de minste tijd heeft en ga die dag glas legen want die duurt langer
				if(totalTime[t-1] < totalTime[t]) {
					if (isPlastic[t-2]) // swap needed before going to recycling facility and afterwards as well
						totalTime[t-1] = totalTime[t-1] + 40;
					toGlassRecycling[t-1] = true;
					glassCollected[t-1] = glassCollected[t-1] - glassCollected[t-2];
					glassCollected[t] = glassCollected[t] - glassCollected[t-2];
					totalTime[t-1] = totalTime[t-1] + 261;
					toPlasticRecycling[t] = true;
					plasticCollected[t] = plasticCollected[t] - plasticCollected[t-1];
					totalTime[t] = totalTime[t] + 113;
				}
				else {
					if (!isPlastic[t-2]) // swap needed before going to recycling facility and afterwards as well
						totalTime[t-1] = totalTime[t-1] + 40;
					toPlasticRecycling[t-1] = true;
					plasticCollected[t-1] = plasticCollected[t-1] - plasticCollected[t-2];
					plasticCollected[t] = plasticCollected[t] - plasticCollected[t-2];
					totalTime[t-1] = totalTime[t-1] + 113;
					toGlassRecycling[t] = true;
					glassCollected[t] = glassCollected[t] - glassCollected[t-1];
					totalTime[t] = totalTime[t] + 281;
					isPlastic[t] = false;
				}
			}
			else if (plasticCollected[t] >= 75 && glassCollected[t] >= 75 && !isPlastic[t]) { // go to both recycling facilities, check on which day which container to empty, kijk naar welke dag de minste tijd heeft en ga die dag glas legen want die duurt langer
				if(totalTime[t-1] < totalTime[t]) {
					if (isPlastic[t-2])
						totalTime[t-1] = totalTime[t-1] + 40;
					toGlassRecycling[t-1] = true;
					glassCollected[t-1] = glassCollected[t-1] - glassCollected[t-2];
					glassCollected[t] = glassCollected[t] - glassCollected[t-2];
					totalTime[t-1] = totalTime[t-1] + 261;
					toPlasticRecycling[t] = true;
					plasticCollected[t] = plasticCollected[t] - plasticCollected[t-1];
					totalTime[t] = totalTime[t] + 133;
					isPlastic[t] = true;
				}
				else {
					if (!isPlastic[t-2]) 
						totalTime[t-1] = totalTime[t-1] + 40;
					toPlasticRecycling[t-1] = true;
					plasticCollected[t-1] = plasticCollected[t-1] - plasticCollected[t-2];
					plasticCollected[t] = plasticCollected[t] - plasticCollected[t-2];
					totalTime[t-1] = totalTime[t-1] + 113;
					toGlassRecycling[t] = true;
					glassCollected[t] = glassCollected[t] - glassCollected[t-1];
					totalTime[t] = totalTime[t] + 261;
				}
			}
			else if (plasticCollected[t] >= 75 && glassCollected[t] < 75) { // swap needed to go to recycling facility
				if (!isPlastic[t]) {
					isPlastic[t] = true;		
					if (totalTime[t-1] <= 460) { // swap at end of previous day
						isPlastic[t-1] = true;
						totalTime[t-1] = totalTime[t-1] + 20;
					}
					else // else today
						totalTime[t] = totalTime[t] + 20;
				}
				toPlasticRecycling[t] = true;
				plasticCollected[t] = plasticCollected[t] - plasticCollected[t-1];	
				totalTime[t] = totalTime[t] + 113;
			}
			else if (glassCollected[t] >= 75 && plasticCollected[t] < 75) { // swap needed to go to recycling facility
				if (isPlastic[t]) {
					isPlastic[t] = false;
					if (totalTime[t-1] <= 460) { // swap at end of previous day
						isPlastic[t-1] = false;
						totalTime[t-1] = totalTime[t-1] + 20;
					}
					else // else today
						totalTime[t] = totalTime[t] + 20;
				}
				toGlassRecycling[t] = true;
				glassCollected[t] = glassCollected[t] - glassCollected[t-1];
				totalTime[t] = totalTime[t] + 261;
			}
			// swap after recycling facility
			if (emptyPlastic && emptyGlass && isPlastic[t]) { // first empty plastic, swap, empty glass
				totalTime[t] = totalTime[t] + 20;
				isPlastic[t] = false;
			}
			else if (emptyPlastic && emptyGlass && !isPlastic[t]) { // first empty glass, swap, empty plastic
				totalTime[t] = totalTime[t] + 20;
				isPlastic[t] = true;
			}
			else if (!emptyPlastic && emptyGlass && isPlastic[t]) { // swap to glass container before emptying
				totalTime[t] = totalTime[t] + 20;
				isPlastic[t] = false;
			}
			else if (emptyPlastic && !emptyGlass && !isPlastic[t]) { // swap to plastic container before emptying
				totalTime[t] = totalTime[t] + 20;
				isPlastic[t] = true;
			}

		}
	}

	public static void addPredictedWaste() {
		for(int i=1; i<nodes; i++) {
			graph.getLocation(i).getGlassContainer().changePredictedAmountGarbage(graph.getLocation(i).getGlassContainer().getMeanGarbageDisposed());
			graph.getLocation(i).getPlasticContainer().changePredictedAmountGarbage(graph.getLocation(i).getPlasticContainer().getMeanGarbageDisposed());
		}
	}

	public static void nextDay() {
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

	public static void output() {
		System.out.println("Total times per day = " + Arrays.toString(totalTime));
		double sum = 0;
		int overTime = 0;
		int timesToPlasticRecycling = 0;
		int timesToGlassRecycling = 0;
		for (int i = 0; i < totalTime.length; i++) {
			sum += totalTime[i];
			if (totalTime[i] > 480) {
				overTime++;
			}
			if (toPlasticRecycling[i] == true)
				timesToPlasticRecycling++;
			if (toGlassRecycling[i] == true)
				timesToGlassRecycling++;
		}
		System.out.println("Objective value = " + sum);
		System.out.println("Number of days working day time constraint is violated = " + overTime);
		System.out.println("Number of times to plastic recycling = " + timesToPlasticRecycling + ", to glass recycling = " + timesToGlassRecycling);
	}
}
