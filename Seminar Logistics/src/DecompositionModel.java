import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ilog.concert.IloException;

/**
 * @author Stein, Marja, Manuela
 */
public class DecompositionModel {
	private CplexModelSchedule scheduleModel;
	private Graph graph;

	private final double ZVALUE =1.645;//= 1.645; //Th
	private final double ALPHA = 1; // Please do not change without thinking very deeply (and asking Marja or Manuela)
	private List<List<Integer>> possibleRoutes;
	private List<Double> averagePDays;
	private List<Double> averageGDays;
	private List<Integer> lastEmptiedPlasticTime;
	private List<Integer> lastEmptiedGlassTime;
	private List<Double> lastEmptiedPlasticAmount;
	private List<Double> lastEmptiedGlassAmount;

	// WARNING: a container is emptied only once within time horizon, so do not set higher than interval emptying time of most emptied container
	private int timeHorizon; //rolling horizon
	private int currentCapPlastic; //ct0
	private int currentCapGlass;
	private boolean hasPlasticContainer;
	private final static int TOTALRUNNINGDAYS=20;
	private final static int MAXCAPACITYCONTAINER=75;
	private final static int RECYCLINGPLASTIC = 113;
	private final static int RECYCLINGGLASS = 261;
	private final static int STARTDAY=2;
	private final static int TIMEHORIZON = 3;
	private final static int MAXTIMEDEV = 3;
	private int runningTime;
	private int day;
	//Statistics:

	
	
	public static void main(String[] args) throws NumberFormatException, IOException, IloException {
		//DecompositionModel model = new DecompositionModel(1,1);
		//model.init();
		//model.scheduleDay();
		Graph graph= Utils.init();

		DecompositionModel model2= new DecompositionModel(graph, "allRoutescluster10.3.csv", "allDistancesPlasticcluster10.3.csv","allDistancesGlasscluster10.3.csv","daysbeforeempty_plastic.csv","daysbeforeempty_glass.csv", TIMEHORIZON, MAXTIMEDEV);
		model2.run();
		System.out.print(model2.runningTime);
		//model2.init();
		//model2.scheduleDay();
	}
	
	public void run() throws IloException {
		init();
		for(day=STARTDAY;day<STARTDAY+TOTALRUNNINGDAYS;day++) {
			if((day-1)%5==0) {
				graph.updateGarbage();
				graph.updateGarbage();
			} 
			graph.updateGarbage();
			scheduleDay();
			//evt stats per day 
		}
		
	}

	// General constructor
	public DecompositionModel(Graph instance, String routeFileName, String plasticDistanceFileName, String glassDistanceFileName, String avgPDaysFileName, String avgGDaysFileName, int timeHorizon, int maxDeviationTime) throws NumberFormatException, IloException, IOException {
		this.averagePDays = Utils.readAverageDays(avgPDaysFileName);
		this.averageGDays = Utils.readAverageDays(avgGDaysFileName);
		scheduleModel = new CplexModelSchedule(instance, routeFileName, plasticDistanceFileName, glassDistanceFileName, timeHorizon, maxDeviationTime);
		ExactSmall.setModel(instance);
		graph = instance;
		possibleRoutes = Utils.readRoutes(routeFileName);
		this.timeHorizon = timeHorizon;
	}
	
	// Short specific constructor with predefined file names
	public DecompositionModel(int timeHorizon, int maxTimeDeviation) throws NumberFormatException, IOException, IloException {
		this.averagePDays = Utils.readAverageDays("daysbeforeempty_plastic.csv");
		this.averageGDays = Utils.readAverageDays("daysbeforeempty_glass.csv");
		graph = Utils.init();
		scheduleModel = new CplexModelSchedule(graph,"allRoutesBasic.csv","allDistancesGlassBasic.csv","allDistancesGlassBasic.csv", timeHorizon, maxTimeDeviation);
		ExactSmall.setModel(graph);
		possibleRoutes = Utils.readRoutes("allRoutesBasic.csv");
		this.timeHorizon = timeHorizon;
	}
	
	public void init() {
		currentCapPlastic=20;
		runningTime=0;
		currentCapGlass=0;
		//graph.initGarbageMean();
		hasPlasticContainer=true;
		initLastEmptiedTime();
		graph.initGarbageUsingT(lastEmptiedPlasticTime,lastEmptiedGlassTime);//TODO
	}
	
	/**
	 * Method that initializes time t tilde, the last time a cube is emptied
	 * Number is chosen such that is uniformly distributed over the average time needed before it should be emptied
	 */
	public void initLastEmptiedTime() {
		// set depot equal to 0
		lastEmptiedPlasticTime= new ArrayList<>();
		lastEmptiedGlassTime=new ArrayList<>();
		lastEmptiedPlasticTime.add(0);
		lastEmptiedGlassTime.add(0);
		for(int i = 1; i < graph.getLocations().size(); i++) {
			int valueP=averagePDays.get(i)<=0.0001?0:-1*graph.getRandom().nextInt((int)Math.ceil(averagePDays.get(i)));
			int valueG=averageGDays.get(i)<=0.0001?0: -1*graph.getRandom().nextInt((int)Math.ceil(averageGDays.get(i)));
			lastEmptiedPlasticTime.add(valueP);
			lastEmptiedGlassTime.add(valueG);
		}
		//Init Z
		lastEmptiedPlasticAmount= new ArrayList<>();
		lastEmptiedGlassAmount=new ArrayList<>();
		for(int i = 0; i < graph.getLocations().size(); i++) {
			lastEmptiedPlasticAmount.add(0.0);
			lastEmptiedGlassAmount.add(0.0);
		}
	}
	
	/** 
	 * In this method we collect all parts, we determine the garbage, we schedule the priorities to right days, we route, we execute and then update for next day
	 * @throws IloException
	 */
	public void scheduleDay() throws IloException {
		//Determine garbage per route
		double[][] garbagePlastic = determineGarbage(true);
		double[][] garbageGlass = determineGarbage(false);
		//Determine priorities per day (for now based on expected garbage per route)
		int[][] priorityPlastic = determinePriority(true);
		int[][] priorityGlass = determinePriority(false);
		
		// Printing methods for printing priorities, these forloops are nonfunctional
		for(int t = 0; t <= timeHorizon; t++) {
			System.out.print("plastic Priority day " + t + ": ");
			for(int i = 0; i < graph.getLocations().size(); i++) {
				if(priorityPlastic[i][t] == 1) {
					System.out.print(i+", ");
				}
			}
			System.out.println();
		}
		for(int t = 0; t <= timeHorizon; t++) {
			System.out.print("glass Priority day " + t + ": ");
			for(int i = 0; i < graph.getLocations().size(); i++) {
				if(priorityGlass[i][t] == 1) {
					System.out.print(i + ", ");
				}
			}
			System.out.println();
		}
		
		//Determine Schedule
		scheduleModel.initModel2(currentCapPlastic, currentCapGlass, garbagePlastic, garbageGlass, priorityPlastic, priorityGlass);
		scheduleModel.solve();
		//System.out.println(scheduleModel.plasticLocToVisit(1));
		//System.out.println(scheduleModel.glassLocToVisit(1));
		//System.out.println(scheduleModel.goToPlasticRecycling());
		
		//Route schedule
		ExactSmall.solve(true,scheduleModel.plasticLocToVisit(1));
		List<Integer> plasticRoute = ExactSmall.getOptimalRoute();
		int distancePlastic=ExactSmall.getOptimalTime();
		ExactSmall.solve(false,scheduleModel.glassLocToVisit(1));
		List<Integer>glassRoute= ExactSmall.getOptimalRoute();
		int distanceGlass=ExactSmall.getOptimalTime();
		
		boolean visitPlasticFacility= (scheduleModel.goToPlasticRecycling().get(0)==1);
		boolean visitGlassFacility= (scheduleModel.goToGlassRecycling().get(0)==1);
		//Execute schedule
		executeRoute(plasticRoute,glassRoute,visitPlasticFacility,visitGlassFacility,distancePlastic,distanceGlass);

		for(int i=0;i<lastEmptiedPlasticTime.size();i++) {
			lastEmptiedPlasticTime.set(i, lastEmptiedPlasticTime.get(i)-1);
			lastEmptiedGlassTime.set(i, lastEmptiedGlassTime.get(i)-1);
		}

	}
	
	/** In this method we determine for each day in the rolling horizon how much garbage is expected to be present
	 * @param isPlastic true if we look for plastic containers, false if we go for glass
	 * @return double array with expected amount of garbage for each route and each day in rolling time.
	 */
	public double[][] determineGarbage(boolean isPlastic) {
		double[][] result = new double[possibleRoutes.size()][timeHorizon+1];
		int r = 0;
		for(List<Integer>route : possibleRoutes) {
			double currentAmount = 0;
			double totalExtraPerDay = 0;
			// Bijhouden hoeveel je per route ophaalt.
			for(Integer loc : route) {
				if(isPlastic) {
					currentAmount += graph.getLocation(loc).getPredictedPlastic();
					totalExtraPerDay += graph.getLocation(loc).getPlasticContainer().getMeanGarbageDisposed();
				} else {
					currentAmount += graph.getLocation(loc).getPredictedGlass();
					totalExtraPerDay += graph.getLocation(loc).getGlassContainer().getMeanGarbageDisposed();
				}
			}
			int mondays=0;
			for(int t = 0; t <= timeHorizon; t++) {
				// We forecast the amount for every point in time by adding the mean t times to the current predicted amount.
				if((day+t-1)%5==0) {//take note of the weekend
					mondays++;
				}
				result[r][t] = currentAmount + t*totalExtraPerDay+2*mondays*totalExtraPerDay;
			}
			r++;
		}
		return result;
	}

	public int[][] determinePriority(boolean isPlastic){
		
		int[][]result = new int[graph.getLocations().size()][timeHorizon+1];
		for(int i = 1; i < graph.getLocations().size(); i++) {
			// This boolean makes sure we only empty a container once in the sliding time window!
			boolean isPriorityPlastic = false;
			boolean isPriorityGlass = false;
			Location loc = graph.getLocation(i);
			
			for(int t = 0; t <= timeHorizon; t++) {
				if(isPlastic && !isPriorityPlastic) {
					int tTilde = lastEmptiedPlasticTime.get(i);
					int weekends = (t - tTilde) / 5; // Floor function not necessary in int value
					int dayT = (this.day+t-1)%5;
					int dayTTilde = (this.day+tTilde-1)%5;
					if(dayTTilde < 0) {
						dayTTilde = dayTTilde + 5;
					}
					if(dayT < dayTTilde) {
						weekends++;
					}
					Container cont = loc.getPlasticContainer();
					double value = cont.getMeanGarbageDisposed() * (t - tTilde + 2*weekends) + ZVALUE *cont.getStdGarbageDisposed() * Math.sqrt(t - tTilde + 2*weekends) + lastEmptiedPlasticAmount.get(i);
					if(value > cont.getCapacity()*ALPHA) {
						isPriorityPlastic = true;
						if(t==0) {
							result[i][1] = 1;
						}
						else {
							result[i][t] = 1;
						}
					}
				} 
				else if (!isPlastic && !isPriorityGlass){
					int tTilde = lastEmptiedPlasticTime.get(i);
					int weekends = (t - tTilde) / 5; // Floor function not necessary in int value
					int dayT = (this.day+t-1)%5;
					int dayTTilde = (this.day+tTilde-1)%5;
					if(dayTTilde < 0) {
						dayTTilde = dayTTilde + 5;
					}
					if(dayT < dayTTilde) {
						weekends++;
					}
					Container cont = loc.getGlassContainer();
					double value = cont.getMeanGarbageDisposed() * (t - tTilde + 2*weekends) + ZVALUE *cont.getStdGarbageDisposed() * Math.sqrt(t - tTilde + 2*weekends) + lastEmptiedGlassAmount.get(i);
					if(value > cont.getCapacity()*ALPHA) {
						isPriorityGlass = true;
						if(t==0) {
							// TODO Check if we do a route on day 0
							result[i][1] = 1;
						}
						else {
							result[i][t] = 1;
						}
					}
				}
			}
		}
		return result;
	}
	
	public void executeRoute(List<Integer>plasticRoute, List<Integer>glassRoute, boolean toPlasticFacility, boolean toGlassFacility, int distPlastic, int distGlass) {
		//Execute recycling
		if(toPlasticFacility) {
			currentCapPlastic=0;
			runningTime+=RECYCLINGPLASTIC;
		}
		if(toGlassFacility) {
			currentCapGlass=0;
			runningTime+=RECYCLINGGLASS;
		}
		//Execute Route
		for(Integer locNumber:plasticRoute) {
			currentCapPlastic+=graph.getLocation(locNumber).emptyPlastic(MAXCAPACITYCONTAINER-currentCapPlastic);
			//update t:
			lastEmptiedPlasticTime.set(locNumber, 0);
			//update z:
			lastEmptiedPlasticAmount.set(locNumber, graph.getLocation(locNumber).getActualPlastic());
		}
		for(Integer locNumber:glassRoute) {
			currentCapGlass+=graph.getLocation(locNumber).emptyGlass(MAXCAPACITYCONTAINER-currentCapGlass);
			//update t:
			lastEmptiedGlassTime.set(locNumber, 0);
			//update z:
			lastEmptiedGlassAmount.set(locNumber, graph.getLocation(locNumber).getActualGlass());
		}
		runningTime+=distPlastic+distGlass;
		//Determine number of swaps
		//Swap 1:
		if(hasPlasticContainer&&toGlassFacility) {
			runningTime+=20;
			hasPlasticContainer=false;
		}else if(!hasPlasticContainer&&toPlasticFacility) {
			runningTime+=20;
			hasPlasticContainer=true;
		}
		//Swap2:
		if(hasPlasticContainer&&glassRoute.size()>0) {
			runningTime+=20;
			hasPlasticContainer=false;
		}else if(!hasPlasticContainer&&plasticRoute.size()>0) {
			runningTime+=20;
			hasPlasticContainer=true;
		}
	}
}