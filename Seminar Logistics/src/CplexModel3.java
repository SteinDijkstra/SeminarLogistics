import java.io.IOException;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CplexModel3 {
	private IloCplex cplex;
	private Graph instance;

	// Variables
	private IloNumVar[][] gPlastic;
	private IloNumVar[][] gGlass;
	private IloNumVar[] cPlastic;
	private IloNumVar[] cGlass;
	private IloNumVar[][] s;
	private IloNumVar[] etaGlass;
	private IloNumVar[] etaPlastic;
	private IloNumVar[][] xPlastic;
	private IloNumVar[][] xGlass;

	// Parameters
	private List<List<Integer>> possibleRoutes;
	private List<Integer> mustSchedule;
	private List<Integer> maySchedule;
	private int timeHorizon;
	private int routes;
	private int nodes;
	double[] distR;
	private double capacityTruck = 75;
	private double recyclingPlastic = 113;
	private double recyclingGlass = 261;
	private double maxTimeUsage = 440;

	public static void main(String[] args) throws NumberFormatException, IloException, IOException {
		//CplexModel2 model= new CplexModel2(Utils.init(),2);
		//model.solve();
		//System.out.println(model.getObjective());
	}

	public CplexModel3(Graph instance, List<List<Integer>> possibleRoutes, List<Integer> mustSchedule, List<Integer> maySchedule, int timeHorizon) throws IloException, NumberFormatException, IOException {
		this.instance=instance;
		this.possibleRoutes = possibleRoutes;
		this.mustSchedule = mustSchedule;
		this.maySchedule = maySchedule;
		this.timeHorizon = timeHorizon;
		this.routes = possibleRoutes.size();
		//this.nodes = instance.getLocations().size();
		this.cplex = new IloCplex();
		// read or implement routes from clustering.
		// distR = Utils.read;

		addVariables();
		//setInitialConditions();
	}

	public void addVariables() throws IloException {
		// Create the correct size matrices
		gPlastic = new IloNumVar[routes][timeHorizon+1];
		gGlass = new IloNumVar[routes][timeHorizon+1];
		cPlastic = new IloNumVar[timeHorizon+1];
		cGlass = new IloNumVar[timeHorizon+1];
		etaPlastic = new IloNumVar[timeHorizon+1];
		etaGlass = new IloNumVar[timeHorizon+1];
		xPlastic = new IloNumVar[routes][timeHorizon+1];
		xGlass = new IloNumVar[routes][timeHorizon+1];

		// Initialize t=0;
		// TODO: Maybe initialize c lower than capacity truck??
		cPlastic[0] = cplex.numVar(0, capacityTruck,"cp0");
		cGlass[0] = cplex.numVar(0, capacityTruck,"cg0");
		etaPlastic[0] = cplex.boolVar("tap0");
		etaGlass[0] = cplex.boolVar("tag0");
		for(int r = 0; r < routes; r++) {
			xPlastic[r][0] = cplex.boolVar("xp" + r + ",0");
			xGlass[r][0] = cplex.boolVar("xg"+r+",0");
			// TODO: Klopt deze bovengrens?
			gPlastic[r][0] = cplex.numVar(0, capacityTruck,"xp" + r + ",0");
			gGlass[r][0] = cplex.numVar(0, capacityTruck,"xg" + r + ",0");
		}

		//for all other periods:
		for(int t = 1; t <= timeHorizon; t++) {
			etaPlastic[t] = cplex.boolVar("tap"+t);
			etaGlass[t] = cplex.boolVar("tag"+t);
			cPlastic[t] = cplex.numVar(0, capacityTruck, "cp" + t);
			cGlass[t] = cplex.numVar(0, capacityTruck, "cg" + t);
			for(int r = 0; r < routes; r++) {
				xPlastic[r][t] = cplex.boolVar("xp" + r + "," + t);
				xGlass[r][t] = cplex.boolVar("xg"+r+","+t);
				gPlastic[r][t] = cplex.numVar(0, capacityTruck,"gp" + r + "," + t);
				gGlass[r][t] = cplex.numVar(0, capacityTruck,"gg" + r + ","+ t );
			}
		}
	}
	
	public void setInitialConditions() throws IloException {
		//Set initial values to 0.
		cplex.addEq(etaPlastic[0], 0);
		cplex.addEq(etaGlass[0], 0);
		cplex.addEq(cPlastic[0], 0);
		cplex.addEq(cGlass[0], 0);
		for(int r = 0; r < nodes; r++) {
			cplex.addEq(xPlastic[r][0], 0);
			cplex.addEq(xGlass[r][0], 0);
			cplex.addEq(gPlastic[r][0], 0);
			cplex.addEq(gGlass[r][0], 0);
		}
	}
	
	public void capacityConstraints() throws IloException {
		// Constraint 1: collect at most capacity
		for (int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sump = cPlastic[t];
			IloNumExpr sumg = cGlass[t];
			for (int r = 0; r < routes; r++) {
				// TODO: check of product variabelen goed gaat.
				// Gaat het echt keer 1?
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t],xPlastic[r][t]));
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
			for(int r = 0; r < routes; r++) {
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t], xPlastic[r][t]));
				sumg = cplex.sum(sumg, cplex.prod(gGlass[r][t], xGlass[r][t]));
			}
			cplex.addLe(cPlastic[t], sump);
			cplex.addLe(cGlass[t], sumg);
		}
		
		// Constraint 4
		for(int t = 1; t <= timeHorizon; t++) {
			IloNumExpr sump = cPlastic[t-1];
			IloNumExpr sumg = cGlass[t-1];
			for(int r = 0; r < routes; r++) {
				sump = cplex.sum(sump, cplex.prod(gPlastic[r][t], xPlastic[r][t]));
				sumg = cplex.sum(sumg, cplex.prod(gGlass[r][t], xGlass[r][t]));
			}
			sump = cplex.sum(sump, cplex.prod(-capacityTruck, etaPlastic[t]));
			sumg = cplex.sum(sumg, cplex.prod(-capacityTruck, etaGlass[t]));
			cplex.addLe(cPlastic[t], sump);
			cplex.addLe(cGlass[t], sumg);
		}
		
		// Constraint 5
		//TODO: constraint is onjuist!! Sommeren over h voor alle t
		IloNumExpr sump = cplex.constant(0);
		IloNumExpr sumg = cplex.constant(0);
		for(int t = 1; t <= timeHorizon; t++) {
			sump = cplex.sum(sump, etaPlastic[t]);
			sumg = cplex.sum(sumg, etaGlass[t]);			
		}
		cplex.addLe(sump, 1);
		cplex.addLe(sumg, 1);
	}

	public void schedulingConstraints() throws IloException {
		for(int i : mustSchedule) {
			IloNumExpr sumpMust = cplex.constant(0);
			IloNumExpr sumgMust = cplex.constant(0);
			for(int t = 1; t <= timeHorizon; t++) {
				for(int r = 0; r < routes; r++) {
					sumpMust = cplex.sum(sumpMust, cplex.prod(s[i][r], xPlastic[r][t]));
					sumgMust = cplex.sum(sumgMust, cplex.prod(s[i][r], xGlass[r][t]));
				}
			}
			cplex.addEq(sumpMust, 1);
			cplex.addEq(sumgMust, 1);
		}
		for(int i : maySchedule) {
			IloNumExpr sumpMust = cplex.constant(0);
			IloNumExpr sumgMust = cplex.constant(0);
			for(int t = 1; t <= timeHorizon; t++) {
				for(int r = 0; r < routes; r++) {
					sumpMust = cplex.sum(sumpMust, cplex.prod(s[i][r], xPlastic[r][t]));
					sumgMust = cplex.sum(sumgMust, cplex.prod(s[i][r], xGlass[r][t]));
				}
			}
			cplex.addLe(sumpMust, 1);
			cplex.addLe(sumgMust, 1);
		}
	}

	public void timeConstraint() throws IloException {
		for(int t=1; t < timeHorizon; t++) {
			IloNumExpr time = cplex.constant(0);
			for (int r = 0; r < routes; r++) {
				time = cplex.sum(time, cplex.prod(distR[r], xPlastic[r][t]));
				time = cplex.sum(time, cplex.prod(distR[r], xGlass[r][t]));
			}
			time = cplex.sum(time, cplex.prod(etaPlastic[t], recyclingPlastic));
			time = cplex.sum(time, cplex.prod(etaGlass[t], recyclingGlass));
			cplex.addLe(time, maxTimeUsage);
		}
	}

	public void addObjective() throws IloException{
		IloNumExpr obj = cplex.constant(0);
		for (int t = 1; t <= timeHorizon; t++) {
			for(int r = 0; r < routes; r++) {
				obj = cplex.sum(obj, cplex.prod(distR[r], xPlastic[r][t]));
				obj = cplex.sum(obj, cplex.prod(distR[r], xGlass[r][t]));
			}
			obj = cplex.sum(obj, cplex.prod(etaPlastic[t], recyclingPlastic)); 
			obj = cplex.sum(obj, cplex.prod(etaGlass[t], recyclingGlass));
		}
		cplex.addMinimize(obj);
	}

}
