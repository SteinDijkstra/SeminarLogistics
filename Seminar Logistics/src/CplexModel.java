import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CplexModel {


	public static void main(String[] args) throws IloException {
		IloCplex cplex = new IloCplex();;
		final Graph graph = null;
		int nodes = graph.getLocations().size();
		int timeHorizon = 1;
		double capacityTruck = 75;
		double m = 20;
		double recyclingPlastic = 113;
		double recyclingGlass = 261;

		// Build the model
		IloNumVar[] etap = new IloNumVar[timeHorizon];
		IloNumVar[] etag = new IloNumVar[timeHorizon];
		IloNumVar[] zp = new IloNumVar[timeHorizon];
		IloNumVar[] zg = new IloNumVar[timeHorizon];
		IloNumVar[] s = new IloNumVar[timeHorizon];
		IloNumVar[] cp = new IloNumVar[timeHorizon];
		IloNumVar[] cg = new IloNumVar[timeHorizon];
		IloNumVar[][] p = new IloNumVar[3][timeHorizon];
		IloNumVar[][] op = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] og = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] gp = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] gg = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] fp = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] fg = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] xp = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][] xg = new IloNumVar[nodes][timeHorizon];
		IloNumVar[][][] yp = new IloNumVar[nodes][nodes][timeHorizon];
		IloNumVar[][][] yg = new IloNumVar[nodes][nodes][timeHorizon];
		IloNumVar[][][] qp = new IloNumVar[nodes][nodes][timeHorizon];
		IloNumVar[][][] qg = new IloNumVar[nodes][nodes][timeHorizon];

		// Variable initialization
		for(int t=0; t < timeHorizon; t++) {
			etap[t] = cplex.boolVar();
			etag[t] = cplex.boolVar();
			zp[t] = cplex.boolVar();
			zg[t] = cplex.boolVar();
			s[t] = cplex.intVar(0, 3);
			cp[t] = cplex.numVar(0, capacityTruck);
			cg[t] = cplex.numVar(0, capacityTruck);
			for (int k=0; k < 3; k++) {
				p[k][t] = cplex.boolVar();
			}
			for(int i=0; i < nodes; i++) {
				xp[i][t] = cplex.numVar(0, capacityTruck);
				xp[i][t] = cplex.numVar(0, capacityTruck);
				op[i][t] = cplex.boolVar();
				op[i][t] = cplex.boolVar();
				gp[i][t] = cplex.boolVar();
				gp[i][t] = cplex.boolVar();
				fp[i][t] = cplex.numVar(0, capacityTruck);
				fp[i][t] = cplex.numVar(0, capacityTruck);
				for(int j=0; j < nodes; j++) {
					yp[i][j][t] = cplex.boolVar();
					yg[i][j][t] = cplex.boolVar();
					qp[i][j][t] = cplex.numVar(0, capacityTruck);
					qg[i][j][t] = cplex.numVar(0, capacityTruck);
				}
			}
		}



		// Objective
		IloNumExpr obj = cplex.constant(0.0);
		for (int t=0; t < timeHorizon; t++) {
			obj = cplex.sum(cplex.prod(m, s[t]), obj);
			obj = cplex.sum(cplex.prod(recyclingPlastic, etap[t]), obj);
			obj = cplex.sum(cplex.prod(recyclingGlass, etag[t]), obj);
			for(int i=0; i < nodes; i++) {
				for(int j=0; j < nodes; j++) {
					if (i != j) {
						obj = cplex.sum(cplex.prod(graph.getDistance(i, j), yp[i][j][t]), obj);
						obj = cplex.sum(cplex.prod(graph.getDistance(i, j), yg[i][j][t]), obj);
					}
					if (i != 0 && j != 0) { // TODO: y fout
						obj = cplex.sum(cplex.prod(graph.getLocation(i).getPlasticEmptyTime(), yp[0][j][t]), obj);
						obj = cplex.sum(cplex.prod(graph.getLocation(i).getGlassEmptyTime(), yg[0][j][t]), obj);
					}
				}
			}
		}
		cplex.addMinimize(obj);

		// Constraint 1
		for(int i=0 ; i < nodes; i++) {
			IloNumExpr expr = cplex.constant(0.0);
			for(int r=0; r < nodes; r++) {
				if (i != r)
					expr = cplex.sum(expr, y[i][r], y[r][i]);
			}
			cplex.addEq(expr, 2);
		}

		// Solve the model
		boolean solved = cplex.solve();

		if(!solved) {
			System.out.println("infeasible");
			return;
		}

		System.out.println("Solved. Objective = " + cplex.getObjValue());

		int sum=0;
		for(int i=0; i<nodes; i++) {
			for (int j=0; j < nodes; j++) {
				if (y[i][j].toString() == "1") 
					sum++;
			}
		}
		System.out.println(sum);
	}



}

