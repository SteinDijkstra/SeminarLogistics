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
		double M = Double.MAX_VALUE;

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

		// Constraint 1 TODO: r bepalen
		for (int t=1; t < timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr exprp = cplex.sum(cplex.sum(fp[i][t-1], cplex.prod(-1, xp[i][t-1])),graph.getLocation(i).getActualPlastic());
				IloNumExpr exprg = cplex.sum(cplex.sum(fg[i][t-1], cplex.prod(-1, xg[i][t-1])),graph.getLocation(i).getActualGlass());
				cplex.addEq(fp[i][t], exprp);
				cplex.addEq(fg[i][t], exprg);
			}
		}

		// Constraint 2
		for (int t=0; t < timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				cplex.addLe(xp[i][t], fp[i][t]);
				cplex.addLe(xg[i][t], fg[i][t]);
			}
		}

		// Constraint 3
		for (int t=0; t < timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr sumyp = cplex.constant(0.0);
				IloNumExpr sumyg = cplex.constant(0.0);
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						sumyp = cplex.sum(sumyp, yp[i][j][t]);
						sumyg = cplex.sum(sumyg, yg[i][j][t]);
					}
				}
				IloNumExpr exprp = cplex.sum(cplex.sum(fp[i][t], cplex.prod(-M, gp[i][t]), cplex.prod(M, sumyp)), -M);
				IloNumExpr exprg = cplex.sum(cplex.sum(fg[i][t], cplex.prod(-M, gg[i][t]), cplex.prod(M, sumyg)), -M);
				cplex.addGe(xp[i][t], exprp);
				cplex.addGe(xg[i][t], exprg);	
			}
		}

		// Constraint 4
		for (int t=0; t < timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr sumyp = cplex.constant(0.0);
				IloNumExpr sumyg = cplex.constant(0.0);
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						sumyp = cplex.sum(sumyp, yp[i][j][t]);
						sumyg = cplex.sum(sumyg, yg[i][j][t]);
					}
				}
				IloNumExpr exprp = cplex.prod(capacityTruck, sumyp);
				IloNumExpr exprg = cplex.prod(capacityTruck, sumyg);
				cplex.addLe(xp[i][t], exprp);
				cplex.addLe(xg[i][t], exprg);
			}
		}

		// Constraint 5
		for (int t=0; t < timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr exprp1 = cplex.sum(capacityTruck, cplex.prod(-1, qp[i][0][t]));
				IloNumExpr exprp2 = cplex.sum(M, cplex.prod(-1, gp[i][t]));
				IloNumExpr exprg1 = cplex.sum(capacityTruck, cplex.prod(-1, qg[i][0][t]));
				IloNumExpr exprg2 = cplex.sum(M, cplex.prod(-1, gg[i][t]));
				cplex.addLe(exprp1, exprp2);
				cplex.addLe(exprg1, exprg2);
			}
		}

		// Constraint 9
		for (int t=1; t < timeHorizon; t++) {
			for (int j=0; j < nodes; j++) {
				IloNumExpr exprp1 = cplex.constant(0.0);
				IloNumExpr exprp2 = cplex.constant(0.0);
				IloNumExpr exprg1 = cplex.constant(0.0);
				IloNumExpr exprg2 = cplex.constant(0.0);
				for (int i=0; i < nodes; i++) {
					if (i != j) {
						exprp1 = cplex.sum(exprp1, yp[i][j][t]);
						exprp2 = cplex.sum(exprp2, yp[j][i][t]);
						exprg1 = cplex.sum(exprg1, yg[i][j][t]);
						exprg2 = cplex.sum(exprg2, yg[j][i][t]);
					}
				}
				cplex.addEq(exprp1, exprp2);
				cplex.addEq(exprg1, exprg2);
			}
		}

		// Constraint 10
		for (int t=1; t < timeHorizon; t++) {
			IloNumExpr exprp = cplex.constant(0.0);
			IloNumExpr exprg = cplex.constant(0.0);
			for (int i=1; i < nodes; i ++) {
				exprp = cplex.sum(exprp, yp[0][i][t]);
				exprg = cplex.sum(exprg, yg[0][i][t]);
			}
			cplex.addLe(exprp, 1);
			cplex.addLe(exprg, 1);
		}

		// Constraint 11
		for (int t=1; t < timeHorizon; t++) {
			for (int j=1; j < nodes; j++) {
				IloNumExpr exprp1 = cplex.constant(0.0);
				IloNumExpr exprp2 = cplex.constant(0.0);
				IloNumExpr exprg1 = cplex.constant(0.0);
				IloNumExpr exprg2 = cplex.constant(0.0);
				for (int i=0; i < nodes; i++) {
					if (i != j) {
						exprp1 = cplex.sum(exprp1, qp[i][j][t]);
						exprp2 = cplex.sum(exprp2, qp[j][i][t]);
						exprg1 = cplex.sum(exprg1, qg[i][j][t]);
						exprg2 = cplex.sum(exprg2, qg[j][i][t]);
					}
				}
				exprp1 = cplex.sum(exprp1, xp[j][t]);
				exprg1 = cplex.sum(exprg1, xg[j][t]);
				cplex.addEq(exprp1, exprp2);
				cplex.addEq(exprg1, exprg2);
			}
		}

		// Constraint 12
		for (int t=1; t < timeHorizon; t++) {
			IloNumExpr exprp1 = qp[0][0][t];
			IloNumExpr exprg1 = qg[0][0][t];
			IloNumExpr exprp2 = cp[t];
			IloNumExpr exprg2 = cg[t];
			for (int i=1; i < nodes; i++) {
				exprp1 = cplex.sum(exprp1, qp[i][0][t]);
				exprg1 = cplex.sum(exprg1, qg[i][0][t]);
				exprp2 = cplex.sum(exprp2, xp[i][t]);
				exprg2 = cplex.sum(exprg2, xg[i][t]);
			}
			cplex.addEq(exprp1, exprp2);
			cplex.addEq(exprg1, exprg2);
		}
		
		// Constraint 13
		for (int t=1; t < timeHorizon; t++) {
			IloNumExpr exprp = cplex.constant(0.0);
			IloNumExpr exprg = cplex.constant(0.0);
			for (int i=0; i < nodes; i++) {
				exprp = cplex.sum(exprp, qp[0][i][t]);
				exprg = cplex.sum(exprg, qg[0][i][t]);
			}
			cplex.addEq(exprp, cp[t]);
			cplex.addEq(exprg, cg[t]);
		}
		
		// Constraint 14
		for (int t=1; t < timeHorizon; t++) {
			for (int i=0; i < nodes; i++) {
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						IloNumExpr exprp = cplex.prod(capacityTruck, yp[i][j][t]);
						IloNumExpr exprg = cplex.prod(capacityTruck, yg[i][j][t]);
						cplex.addLe(qp[i][j][t], exprp);
						cplex.addLe(qg[i][j][t], exprg);
					}
				}
			}
		}
		
		// Constraint 15
		for (int t=1; t < timeHorizon; t++) {
			IloNumExpr sumyp = cplex.constant(0.0);
			IloNumExpr sumyg = cplex.constant(0.0);
			for (int i=0; i < nodes; i++) {
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						sumyp = cplex.sum(sumyp, yp[i][j][t]);
						sumyg = cplex.sum(sumyg, yg[i][j][t]);
					}
				}
			}
			IloNumExpr exprp = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, sumyp));
			IloNumExpr exprg = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, sumyg));
			cplex.addLe(qp[0][0][t], exprp);
			cplex.addLe(qg[0][0][t], exprg);
		}

		// laat staan
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

