import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

public class CplexModelSchedule {
	private IloCplex cplex;
	private Graph instance;

	// Variables
	private IloNumVar[][] gPlastic;
	private IloNumVar[][] gGlass;
	private IloNumVar[] cPlastic;
	private IloNumVar[] cGlass;
	private IloNumVar[] etaGlass;
	private IloNumVar[] etaPlastic;
	private IloNumVar[][] xPlastic;
	private IloNumVar[][] xGlass;
	private IloNumVar[][] priorityPlastic;
	private IloNumVar[][] priorityGlass;

	// Parameters
	private List<List<Integer>> possibleRoutes;
	private List<Integer>glassDistances;
	private List<Integer>plasticDistances;
	
	private int timeHorizon;
	private int maxDeviationTime;
	private int numberOfRoutes;
	private int nodes;
	private double capacityTruck = 75;
	private double recyclingPlastic = 113;
	private double recyclingGlass = 261;
	private double maxTimeUsage = 480;

//	public static void main(String[] args) throws NumberFormatException, IloException, IOException {
//		CplexModelSchedule model = new CplexModelSchedule(Utils.init(),"allRoutesBasic.csv","allDistancesGlassBasic.csv","allDistancesGlassBasic.csv",1,1);
//		double [][] garbagePlastic = new double[208][2];
//		garbagePlastic[1][1]=1.5;
//		double [][] garbageGlass = new double[208][2];
//		garbageGlass[1][1]=1.0;
//		int [][] plasticPriority = new int[208][2];
//		plasticPriority[1][1]=1;
//		int [][] glassPriority = new int[208][2];
//		glassPriority[1][1]=1;
//		int[] lastXPlastic= new int[208];
//		int [] lastXGlass= new int[208];
//		model.initModel(0, 0, garbagePlastic, garbageGlass, plasticPriority, glassPriority, lastXPlastic, lastXGlass);
//		//CplexModel2 model= new CplexModel2(Utils.init(),2);
//		model.solve();
//		System.out.println(model.getObjective());
//		//System.out.println(model.schedulePlastic());
//	}

	public CplexModelSchedule(Graph instance, String routeFileName, String plasticDistanceFileName,String glassDistanceFileName, int timeHorizon,int maxTimeDeviation) throws IloException, NumberFormatException, IOException {
		this.instance=instance;
		this.possibleRoutes = Utils.readRoutes(routeFileName);
		this.glassDistances = Utils.readDistances(glassDistanceFileName);
		this.plasticDistances=Utils.readDistances(plasticDistanceFileName);
		this.timeHorizon = timeHorizon;
		this.numberOfRoutes = possibleRoutes.size();
		this.nodes = instance.getLocations().size();
		this.cplex = new IloCplex();
		this.maxDeviationTime = maxTimeDeviation;
		// read or implement routes from clustering.
		// distR = Utils.read;

		addVariables();
		capacityConstraints();
		schedulingConstraints();
		timeConstraint();
		addObjective();
		//cplex.exportModel("schedule.lp");
		
	}

	public void addVariables() throws IloException {
		// Create the correct size matrices
		gPlastic = new IloNumVar[numberOfRoutes][timeHorizon+1];
		gGlass = new IloNumVar[numberOfRoutes][timeHorizon+1];
		cPlastic = new IloNumVar[timeHorizon+1];
		cGlass = new IloNumVar[timeHorizon+1];
		etaPlastic = new IloNumVar[timeHorizon+1];
		etaGlass = new IloNumVar[timeHorizon+1];
		xPlastic = new IloNumVar[numberOfRoutes][timeHorizon+1];
		xGlass = new IloNumVar[numberOfRoutes][timeHorizon+1];
		priorityPlastic = new IloNumVar[nodes][timeHorizon+1];
		priorityGlass = new IloNumVar[nodes][timeHorizon+1];

		// Initialize t=0;
		// TODO: Maybe initialize c lower than capacity truck??
		cPlastic[0] = cplex.numVar(0, capacityTruck,"cp0");
		cGlass[0] = cplex.numVar(0, capacityTruck,"cg0");
		for(int r = 0; r < numberOfRoutes; r++) {
			xPlastic[r][0] = cplex.boolVar("xp" + r + "," + 0);
			xGlass[r][0] = cplex.boolVar("xg"+r+","+0);
			gPlastic[r][0] = cplex.numVar(0, capacityTruck,"gp" + r + "," + 0);
			gGlass[r][0] = cplex.numVar(0, capacityTruck,"gg" + r + ","+ 0);
		}
		for(int i = 1; i < nodes; i++) {
			priorityPlastic[i][0]=cplex.boolVar("pp" + i + "," + 0);
			priorityGlass[i][0]=cplex.boolVar("pg" + i + "," + 0);
		}
		//for all other periods:
		for(int t = 1; t <= timeHorizon; t++) {
			etaPlastic[t] = cplex.boolVar("tap"+t);
			etaGlass[t] = cplex.boolVar("tag"+t);
			cPlastic[t] = cplex.numVar(0, capacityTruck, "cp" + t);
			cGlass[t] = cplex.numVar(0, capacityTruck, "cg" + t);
			for(int r = 0; r < numberOfRoutes; r++) {
				xPlastic[r][t] = cplex.boolVar("xp" + r + "," + t);
				xGlass[r][t] = cplex.boolVar("xg"+r+","+t);
				gPlastic[r][t] = cplex.numVar(0, capacityTruck,"gp" + r + "," + t);
				gGlass[r][t] = cplex.numVar(0, capacityTruck,"gg" + r + ","+ t );
			}
			for(int i = 1; i < nodes; i++) {
				priorityPlastic[i][t] = cplex.boolVar("pp" + i + "," + t);
				priorityGlass[i][t] = cplex.boolVar("pg" + i + "," + t);
			}
		}
	}
	
	public void initModel(double initCapPlastic, double initCapGlass, double[][]garbagePlastic,double[][]garbageGlass, int[][]plasticPriority, int[][] glassPriority, int[]lastXPlastic, int[]lastXGlass) throws IloException {
		//Set initial values to 0.
		cPlastic[0].setLB(initCapPlastic);
		cPlastic[0].setUB(initCapPlastic);
		cGlass[0].setLB(initCapGlass);
		cGlass[0].setUB(initCapGlass);
		for(int r = 0; r < numberOfRoutes;r++) {
			xPlastic[r][0].setLB(lastXPlastic[r]);xPlastic[r][0].setUB(lastXPlastic[r]);
			xGlass[r][0].setLB(lastXGlass[r]);xGlass[r][0].setUB(lastXGlass[r]);
		}
		for(int t = 0; t <= timeHorizon; t++) {
			for(int r = 0; r < numberOfRoutes; r++) {
				gPlastic[r][t].setLB(garbagePlastic[r][t]);
				gPlastic[r][t].setUB(garbagePlastic[r][t]);
				gGlass[r][t].setLB(garbageGlass[r][t]);
				gGlass[r][t].setUB(garbageGlass[r][t]);
			}
		}
		for(int t = 1; t <= timeHorizon; t++) {
			for(int i = 1; i < nodes; i++) {
				if(plasticPriority[i][t]==0) {	
					priorityPlastic[i][t].setLB(0);
					priorityPlastic[i][t].setUB(1);
				} else {
					priorityPlastic[i][t].setLB(1);
					priorityPlastic[i][t].setUB(1);
				}
				if(glassPriority[i][t]==0) {
					priorityGlass[i][t].setLB(0);
					priorityGlass[i][t].setUB(1);
				} else {
					priorityGlass[i][t].setLB(1);
					priorityGlass[i][t].setUB(1);
				}
			}
		}
		cplex.exportModel("schedule.lp");
	}

	public void initModel2(double initCapPlastic, double initCapGlass, double[][]garbagePlastic,double[][]garbageGlass, int[][]plasticPriority, int[][] glassPriority) throws IloException {
		int[]lastXPlastic = new int[209];
		int[]lastXGlass = new int[209];
		initModel(initCapPlastic, initCapGlass,garbagePlastic,garbageGlass, plasticPriority,glassPriority,lastXPlastic,lastXGlass);
		
	}
	
	public void capacityConstraints() throws IloException {
		// Constraint 1: collect at most capacity
		for (int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sump = cPlastic[t];
			IloNumExpr sumg = cGlass[t];
			for (int r = 0; r < numberOfRoutes; r++) {
				// TODO: check of product variabelen goed gaat.
				// Gaat het echt keer 1?
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t], xPlastic[r][t]));
				sumg = cplex.sum(sumg, cplex.prod(gGlass[r][t], xGlass[r][t]));
			}
			cplex.addLe(sump, capacityTruck);
			cplex.addLe(sumg, capacityTruck);
		}
		
		
		// Constraint 2
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr restrp = cplex.constant(0);
			IloNumExpr restrg = cplex.constant(0);
			restrp = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, etaPlastic[t]));
			restrg = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, etaGlass[t]));
			cplex.addLe(cPlastic[t], restrp);
			cplex.addLe(cGlass[t], restrg);
		}
		
		// Constraint 3
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sump = cPlastic[t-1];
			IloNumExpr sumg = cGlass[t-1];
			for(int r = 0; r < numberOfRoutes; r++) {
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t-1], xPlastic[r][t-1]));
				sumg = cplex.sum(sumg, cplex.prod(gGlass[r][t-1], xGlass[r][t-1]));
			}
			cplex.addLe(cPlastic[t], sump);
			cplex.addLe(cGlass[t], sumg);
		}
		
		// Constraint 4
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sump = cPlastic[t-1];
			IloNumExpr sumg = cGlass[t-1];
			for(int r = 0; r < numberOfRoutes; r++) {
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t-1], xPlastic[r][t-1]));
				sumg = cplex.sum(sumg, cplex.prod(gGlass[r][t-1], xGlass[r][t-1]));
			}
			sump = cplex.sum(sump, cplex.prod(-capacityTruck, etaPlastic[t]));
			sumg = cplex.sum(sumg, cplex.prod(-capacityTruck, etaGlass[t]));
			cplex.addGe(cPlastic[t], sump);
			cplex.addGe(cGlass[t], sumg);
		}	
		
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sumh = cplex.constant(0);
			sumh = cplex.sum(etaGlass[t], etaPlastic[t]);
			cplex.addLe(sumh, 1);
		}
		
	}

	public void schedulingConstraints() throws IloException {
		for(int i = 1; i < nodes; i++) {
			for(int t = 1; t <= timeHorizon; t++) {
				IloNumExpr sumpMust=cplex.constant(0);
				IloNumExpr sumgMust=cplex.constant(0);
				for(int k = 0; k <= maxDeviationTime; k++) {
					if(t - k > 0) {
						for(int r = 0; r < numberOfRoutes; r++) {
							if(possibleRoutes.get(r).contains(i)) {
								sumpMust = cplex.sum(sumpMust, xPlastic[r][t-k]);
								sumgMust = cplex.sum(sumgMust, xGlass[r][t-k]);
							}
						}
					}
				}
				cplex.addEq(sumpMust, priorityPlastic[i][t]);
				cplex.addEq(sumgMust, priorityGlass[i][t]);
			}
		}
	}

	public void timeConstraint() throws IloException {
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr time = cplex.constant(0);
			for (int r = 0; r < numberOfRoutes; r++) {
				time = cplex.sum(time, cplex.prod(plasticDistances.get(r), xPlastic[r][t]));
				time = cplex.sum(time, cplex.prod(glassDistances.get(r), xGlass[r][t]));
			}
			time = cplex.sum(time, cplex.prod(etaPlastic[t], recyclingPlastic));
			time = cplex.sum(time, cplex.prod(etaGlass[t], recyclingGlass));
			cplex.addLe(time, maxTimeUsage);
		}
	}

	public void addObjective() throws IloException{
		IloNumExpr obj = cplex.constant(0);
		for (int t = 1; t <= timeHorizon; t++) {
			for(int r = 0; r < numberOfRoutes; r++) {
				obj = cplex.sum(obj, cplex.prod(plasticDistances.get(r), xPlastic[r][t]));
				obj = cplex.sum(obj, cplex.prod(glassDistances.get(r), xGlass[r][t]));
			}
			obj = cplex.sum(obj, cplex.prod(etaPlastic[t], recyclingPlastic)); 
			obj = cplex.sum(obj, cplex.prod(etaGlass[t], recyclingGlass));
		}
		cplex.addMinimize(obj);
	}
	
	public void solve() throws IloException {
		cplex.solve();
	}
	
	public double getObjective() throws IloException {
		return cplex.getObjValue();
	}
	
	public List<Integer> plasticLocToVisit(int day) throws UnknownObjectException, IloException{
		List<Integer> result= new ArrayList<>();
		for(int r = 0; r < numberOfRoutes; r++) {
			if(cplex.getValue(xPlastic[r][day]) > 0.5) {
				result.addAll(possibleRoutes.get(r));
			}
		}
		return result;
	}
	
	public List<Integer> glassLocToVisit(int day) throws UnknownObjectException, IloException{
		List<Integer> result = new ArrayList<>();
		for(int r = 0; r < numberOfRoutes; r++) {
			if(cplex.getValue(xGlass[r][day]) > 0.5) {
				result.addAll(possibleRoutes.get(r));
			}
		}
		return result;
	}
}