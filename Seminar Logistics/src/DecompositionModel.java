import java.io.IOException;
import java.util.List;

import ilog.concert.IloException;

public class DecompositionModel {
	private CplexModelSchedule scheduleModel;
	private Graph graph;
	private List<List<Integer>>possibleRoutes;
	private int timeHorizon;
	private int currentCapPlastic;
	private int currentCapGlass;
	
	public static void main(String[] args) throws NumberFormatException, IOException, IloException {
		DecompositionModel model = new DecompositionModel(12,4);
		model.init();
		model.scheduleDay();
		
	}
	
	public DecompositionModel(Graph instance, String routeFileName, String plasticDistanceFileName,String glassDistanceFileName, int timeHorizon, int maxDeviationTime) throws NumberFormatException, IloException, IOException {
		scheduleModel=new CplexModelSchedule( instance, routeFileName, plasticDistanceFileName, glassDistanceFileName, timeHorizon, maxDeviationTime);
		ExactSmall.setModel(instance);
		graph=instance;
		this.timeHorizon=timeHorizon;
	}
	public DecompositionModel(int timeHorizon, int maxTimeDeviation) throws NumberFormatException, IOException, IloException {
		graph= Utils.init();
		scheduleModel=new CplexModelSchedule(graph,"allRoutesBasic.csv","allDistancesGlassBasic.csv","allDistancesGlassBasic.csv",timeHorizon, maxTimeDeviation);
		ExactSmall.setModel(graph);
		possibleRoutes=Utils.readRoutes("allRoutesBasic.csv");
		this.timeHorizon=timeHorizon;
	}
	
	public void init() {
		graph.initGarbage();
	}
	
	public void scheduleDay() throws IloException {
		//Determine garbage per route
		double[][] garbagePlastic= determineGarbage(true);
		double[][] garbageGlass= determineGarbage(false);
		//Determine priorities per day (for no based on expected garbage per route
		int[][] priorityPlastic=determinePriority(true);
		for(int t=0;t<=timeHorizon;t++) {
			System.out.println("plastic Priority day "+t+": ");
			for(int i=0;i<graph.getLocations().size()-1;i++) {
				if(priorityPlastic[i][t]==1) {
					System.out.println((i+1)+", ");
				}
			}
		}
		int[][] priorityGlass=determinePriority(false);
		for(int t=0;t<=timeHorizon;t++) {
			System.out.println("glass Priority day "+t+": ");
			for(int i=0;i<graph.getLocations().size()-1;i++) {
				if(priorityGlass[i][t]==1) {
					System.out.println((i+1)+", ");
				}
			}
		}
		//Determine Schedule
		scheduleModel.initModel2(currentCapPlastic, currentCapGlass, garbagePlastic, garbageGlass, priorityPlastic, priorityGlass);
		scheduleModel.solve();
		
		System.out.println(scheduleModel.plasticLocToVisit(1));
		System.out.println(scheduleModel.glassLocToVisit(1));
		//Route schedule
		
		//Execute schedule
		
		//update for next day
		
	}
	public double[][] determineGarbage(boolean isPlastic) {
		double[][] result = new double[possibleRoutes.size()][timeHorizon+1];
		int r=0;
		for(List<Integer>route:possibleRoutes) {
			double currentAmount=0;
			double totalExtraPerDay=0;
			for(Integer loc:route) {
				if(isPlastic) {
					currentAmount+=graph.getLocation(loc).getPredictedPlastic();
					totalExtraPerDay+=graph.getLocation(loc).getPlasticContainer().getMeanGarbageDisposed();
				} else {
					currentAmount+=graph.getLocation(loc).getPredictedGlass();
					totalExtraPerDay+=graph.getLocation(loc).getGlassContainer().getMeanGarbageDisposed();
				}
			}
			for(int i=0;i<=timeHorizon;i++) {
				result[r][i]=currentAmount+i*totalExtraPerDay;
			}
			r++;
		}
		return result;
	}
	
	public int[][] determinePriority(boolean isPlastic){
		int[][]result= new int[graph.getLocations().size()][timeHorizon+1];
		for(int i=1;i<graph.getLocations().size();i++) {
			boolean isPriority=false;
			Location loc=graph.getLocation(i);
			for(int t=0;t<=timeHorizon;t++) {
				if(isPlastic) {
					Container cont=loc.getPlasticContainer();
					if(loc.getPredictedPlastic()+t*cont.getMeanGarbageDisposed()>cont.getCapacity() &&!isPriority ) {
						isPriority=true;
						result[i-1][t]=1;
					} else {
						result[i-1][t]=0;
					}
				} else {
					Container cont=loc.getGlassContainer();
					if(loc.getPredictedPlastic()+t*cont.getMeanGarbageDisposed()>cont.getCapacity() &&!isPriority ) {
						isPriority=true;
						result[i-1][t]=1;
					} else {
						result[i-1][t]=0;
					}
				}
			}
		}
		return result;
	}


}
