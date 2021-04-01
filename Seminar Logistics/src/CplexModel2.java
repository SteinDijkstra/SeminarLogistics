import java.io.IOException;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

public class CplexModel2 {
	private IloNumVar[] etaPlastic;
	private IloNumVar[] etaGlass;
	private IloNumVar[] zPlastic;
	private IloNumVar[] zGlass;
	private IloNumVar[] s;
	private IloNumVar[] cPlastic;
	private IloNumVar[] cGlass;
	private IloNumVar[][] p;
	private IloNumVar[][] oPlastic;
	private IloNumVar[][] oGlass;
	private IloNumVar[][] gPlastic;
	private IloNumVar[][] gGlass;
	private IloNumVar[][] fPlastic;
	private IloNumVar[][] fGlass;
	private IloNumVar[][] xPlastic;
	private IloNumVar[][] xGlass;
	private IloNumVar[][][] yPlastic;
	private IloNumVar[][][] yGlass;
	private IloNumVar[][][] qPlastic;
	private IloNumVar[][][] qGlass;
	
	private Graph instance;
	private IloCplex cplex;
	private int timeHorizon;
	private int nodes;
	double[][] rGlass;
	double[][] rPlastic;
	
	private double capacityTruck= 75;
	private double m = 20;
	private double recyclingPlastic = 113;
	private double recyclingGlass = 261;
	private double M = 100000;

	
	public static void main(String[] args) throws NumberFormatException, IloException, IOException {
		CplexModel2 model= new CplexModel2(Utils.init(),1);
		model.solve();
		model.debug();
		//System.out.println(model.cplex.getValue(model.qPlastic[0][0][1]));
		//System.out.println(model.getObjective());
	}
	
	public void debug() throws UnknownObjectException, IloException {
		//System.out.println("cp0 "+cplex.getValue(cPlastic[0]));
		System.out.println("cp1 "+cplex.getValue(cPlastic[1]));
		System.out.println("cg0 "+cplex.getValue(cGlass[0]));
		//System.out.println("cg1 "+cplex.getValue(cGlass[1]));
//		System.out.println("etap1 "+cplex.getValue(etaPlastic[1]));
//		//System.out.println("etap0 "+cplex.getValue(etaPlastic[0]));
//		System.out.println("cg0 "+cplex.getValue(cGlass[0]));
//		System.out.println("cg1 "+cplex.getValue(cGlass[1]));
		System.out.println("qp000 "+cplex.getValue(qPlastic[0][0][0]));
		//System.out.println("qp001 "+cplex.getValue(qPlastic[0][0][1]));

//		System.out.println("yp031 "+cplex.getValue(yPlastic[0][3][1]));
//		System.out.println("yg031 "+cplex.getValue(yGlass[0][3][1]));
//		double sum=0;
//		for(int i=0;i<nodes;i++) {
//			System.out.println("qg0"+i+"0 "+cplex.getValue(qGlass[0][i][0]));
//			sum+=cplex.getValue(qGlass[0][i][0]);
//		}
		//System.out.println(sum);
		//System.out.println("zp0 "+cplex.getValue(zPlastic[0]));
		//System.out.println("zp1 "+cplex.getValue(zPlastic[1]));
	}
	
	public CplexModel2(Graph instance,int timeHorizon) throws IloException, NumberFormatException, IOException {
		this.timeHorizon=timeHorizon;
		this.instance=instance;
		this.nodes=instance.getLocations().size();
		this.cplex=new IloCplex();
		rGlass = Utils.readDeposits("glass_deposits3.csv");
		rPlastic = Utils.readDeposits("plastic_deposits3.csv");
		
		addVariables();
		setInitialConditions();
		addAmountOfGarbageCollectedConstraint();
		addRouteConstructionConstraint();
		addGarbageCollectedDuringDayConstraint();
		addOverflowConstraint();
		addBeginingOfDayCapacityConstraint();
		addSwapsConstraint();
		addMaxWorkingDayConstraint();
		addObjective();
		cplex.exportModel("model2.lp");
	}
	
	
	public void addVariables() throws IloException {
		// Create the correct size matrices
		etaPlastic = new IloNumVar[timeHorizon+1];
		etaGlass = new IloNumVar[timeHorizon+1];
		zPlastic = new IloNumVar[timeHorizon+1];
		zGlass = new IloNumVar[timeHorizon+1];
		s = new IloNumVar[timeHorizon+1];
		cPlastic = new IloNumVar[timeHorizon+1];
		cGlass = new IloNumVar[timeHorizon+1];
		p = new IloNumVar[3][timeHorizon+1];
		oPlastic = new IloNumVar[nodes][timeHorizon+1];
		oGlass = new IloNumVar[nodes][timeHorizon+1];
		gPlastic = new IloNumVar[nodes][timeHorizon+1];
		gGlass = new IloNumVar[nodes][timeHorizon+1];
		fPlastic = new IloNumVar[nodes][timeHorizon+1];
		fGlass = new IloNumVar[nodes][timeHorizon+1];
		xPlastic = new IloNumVar[nodes][timeHorizon+1];
		xGlass = new IloNumVar[nodes][timeHorizon+1];
		yPlastic = new IloNumVar[nodes][nodes][timeHorizon+1];
		yGlass = new IloNumVar[nodes][nodes][timeHorizon+1];
		qPlastic = new IloNumVar[nodes][nodes][timeHorizon+1];
		qGlass = new IloNumVar[nodes][nodes][timeHorizon+1];

		// Initialize t=0;
		
		cPlastic[0] = cplex.numVar(0, capacityTruck,"cp0");
		cGlass[0] = cplex.numVar(0, capacityTruck,"cg0");
		zPlastic[0] = cplex.boolVar("zp0");
		zGlass[0] = cplex.boolVar("zg0");
		for(int i=0; i<nodes;i++) {
			fPlastic[i][0] = cplex.numVar(0, capacityTruck,"fp"+i+",0");
			fGlass[i][0] = cplex.numVar(0, capacityTruck,"fg"+i+",0");
			xPlastic[i][0] = cplex.numVar(0, capacityTruck,"xp"+i+",0");
			xGlass[i][0] = cplex.numVar(0, capacityTruck,"xg"+i+",0");
			for(int j=0; j < nodes; j++) {
				qPlastic[i][j][0] = cplex.numVar(0, capacityTruck,"qp"+i+","+j+","+0);
				qGlass[i][j][0] = cplex.numVar(0, capacityTruck,"qg"+i+","+j+","+0);
			}
		}
		//for all other periods:
		for(int t=1; t <= timeHorizon; t++) {
			etaPlastic[t] = cplex.boolVar("tap"+t);
			etaGlass[t] = cplex.boolVar("tag"+t);
			zPlastic[t] = cplex.boolVar("zp"+t);
			zGlass[t] = cplex.boolVar("zg"+t);
			s[t] = cplex.intVar(0, 3,"s"+t);
			cPlastic[t] = cplex.numVar(0, capacityTruck,"cp"+t);
			cGlass[t] = cplex.numVar(0, capacityTruck,"cg"+t);
			for (int k=0; k < 3; k++) {
				p[k][t] = cplex.boolVar("p"+k+","+t);
			}
			for(int i=0; i < nodes; i++) {
				xPlastic[i][t] = cplex.numVar(0, capacityTruck,"xp"+i+","+t);
				xGlass[i][t] = cplex.numVar(0, capacityTruck,"xg"+i+","+t);
				oPlastic[i][t] = cplex.boolVar("op"+i+","+t);
				oGlass[i][t] = cplex.boolVar("og"+i+","+t);
				gPlastic[i][t] = cplex.boolVar("gp"+i+","+t);
				gGlass[i][t] = cplex.boolVar("gg"+i+","+t);
				fPlastic[i][t] = cplex.numVar(0, capacityTruck,"fp"+i+","+t);
				fGlass[i][t] = cplex.numVar(0, capacityTruck,"fg"+i+","+t);
				for(int j=0; j < nodes; j++) {
					yPlastic[i][j][t] = cplex.boolVar("yp"+i+","+j+","+t);
					yGlass[i][j][t] = cplex.boolVar("yg"+i+","+j+","+t);
					qPlastic[i][j][t] = cplex.numVar(0, capacityTruck,"qp"+i+","+j+","+t);
					qGlass[i][j][t] = cplex.numVar(0, capacityTruck,"qg"+i+","+j+","+t);
				}
			}
		}
	}

	public void setInitialConditions() throws IloException {
		//Set initial c
		double initPlastic=10;
		double initGlass=10;
		//cplex.addEq(cPlastic[0], initPlastic);
		//cplex.addEq(cGlass[0], initGlass);
		//Set Initial value z
		cplex.addEq(zPlastic[0],0.0);
		cplex.addEq(zGlass[0], 1.0);
		//Set Init value for f
		for(int i=1;i<nodes;i++) {
			cplex.addEq(fPlastic[i][0],rPlastic[0][i-1]);
			cplex.addEq(fGlass[i][0],rGlass[0][i-1]);
		}
		for(int i=0;i<nodes;i++) {
			cplex.addEq(xPlastic[i][0],0);
			cplex.addEq(xGlass[i][0],0);
			for(int j=0; j < nodes; j++) {
				if(i==0&&j==0) {
					cplex.addEq(qPlastic[i][j][0],initPlastic);
					cplex.addEq(qGlass[i][j][0],initGlass);
				} else {
					cplex.addEq(qPlastic[i][j][0],0);
					cplex.addEq(qGlass[i][j][0],0);
				}
			}
		}
	}
	
	public void addAmountOfGarbageCollectedConstraint() throws IloException {
		// Constraint 1
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr exprp = cplex.sum(cplex.sum(fPlastic[i][t-1], cplex.prod(-1, xPlastic[i][t-1])), rPlastic[t][i-1]);
				// IloNumExpr exprp = cplex.sum(cplex.sum(fp[i][t-1], cplex.prod(-1, xp[i][t-1])),rp[t][i]);
				IloNumExpr exprg = cplex.sum(cplex.sum(fGlass[i][t-1], cplex.prod(-1, xGlass[i][t-1])), rGlass[t][i-1]);
				cplex.addEq(fPlastic[i][t], exprp,"updatefp"+i+","+t);
				cplex.addEq(fGlass[i][t], exprg,"updatefg"+i+","+t);
			}
		}

		// Constraint 2
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				cplex.addLe(xPlastic[i][t], fPlastic[i][t],"maxxp"+i+","+t);
				cplex.addLe(xGlass[i][t], fGlass[i][t],"maxxg"+i+","+t);
			}
		}

		// Constraint 3
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr sumyp = cplex.constant(0.0);
				IloNumExpr sumyg = cplex.constant(0.0);
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						sumyp = cplex.sum(sumyp, yPlastic[i][j][t]);
						sumyg = cplex.sum(sumyg, yGlass[i][j][t]);
					}
				}
				IloNumExpr exprp = cplex.sum(cplex.sum(fPlastic[i][t], cplex.prod(-M, gPlastic[i][t]), cplex.prod(M, sumyp)), -M);
				IloNumExpr exprg = cplex.sum(cplex.sum(fGlass[i][t], cplex.prod(-M, gGlass[i][t]), cplex.prod(M, sumyg)), -M);
				cplex.addGe(xPlastic[i][t], exprp);
				cplex.addGe(xGlass[i][t], exprg);	
			}
		}

		// Constraint 4
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr sumyp = cplex.constant(0.0);
				IloNumExpr sumyg = cplex.constant(0.0);
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						sumyp = cplex.sum(sumyp, yPlastic[i][j][t]);
						sumyg = cplex.sum(sumyg, yGlass[i][j][t]);
					}
				}
				IloNumExpr exprp = cplex.prod(capacityTruck, sumyp);
				IloNumExpr exprg = cplex.prod(capacityTruck, sumyg);
				cplex.addLe(xPlastic[i][t], exprp);
				cplex.addLe(xGlass[i][t], exprg);
			}
		}

		// Constraint 5
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=1; i < nodes; i++) {
				IloNumExpr exprp1 = cplex.sum(capacityTruck, cplex.prod(-1, qPlastic[i][0][t]));
				IloNumExpr exprp2 = cplex.sum(M, cplex.prod(-M, gPlastic[i][t]));
				IloNumExpr exprg1 = cplex.sum(capacityTruck, cplex.prod(-1, qGlass[i][0][t]));
				IloNumExpr exprg2 = cplex.sum(M, cplex.prod(-M, gGlass[i][t]));
				cplex.addLe(exprp1, exprp2);
				cplex.addLe(exprg1, exprg2);
			}
		}
	}
	
	public void addRouteConstructionConstraint() throws IloException {
		// Constraint 9
				for (int t=1; t <= timeHorizon; t++) {
					for (int j=0; j < nodes; j++) {
						IloNumExpr exprp1 = cplex.constant(0.0);
						IloNumExpr exprp2 = cplex.constant(0.0);
						IloNumExpr exprg1 = cplex.constant(0.0);
						IloNumExpr exprg2 = cplex.constant(0.0);
						for (int i=0; i < nodes; i++) {
							if (i != j) {
								exprp1 = cplex.sum(exprp1, yPlastic[i][j][t]);
								exprp2 = cplex.sum(exprp2, yPlastic[j][i][t]);
								exprg1 = cplex.sum(exprg1, yGlass[i][j][t]);
								exprg2 = cplex.sum(exprg2, yGlass[j][i][t]);
							}
						}
						cplex.addEq(exprp1, exprp2);
						cplex.addEq(exprg1, exprg2);
					}
				}

				// Constraint 10
				for (int t=1; t <= timeHorizon; t++) {
					IloNumExpr exprp = cplex.constant(0.0);
					IloNumExpr exprg = cplex.constant(0.0);
					for (int i=1; i < nodes; i ++) {
						exprp = cplex.sum(exprp, yPlastic[0][i][t]);
						exprg = cplex.sum(exprg, yGlass[0][i][t]);
					}
					cplex.addLe(exprp, 1);
					cplex.addLe(exprg, 1);
				}
	}
	
	public void addGarbageCollectedDuringDayConstraint() throws IloException {
		// Constraint 11
		for (int t=1; t <= timeHorizon; t++) {
			for (int j=1; j < nodes; j++) {
				IloNumExpr exprp1 = cplex.constant(0.0);
				IloNumExpr exprp2 = cplex.constant(0.0);
				IloNumExpr exprg1 = cplex.constant(0.0);
				IloNumExpr exprg2 = cplex.constant(0.0);
				for (int i=0; i < nodes; i++) {
					if (i != j) {
						exprp1 = cplex.sum(exprp1, qPlastic[i][j][t]);
						exprp2 = cplex.sum(exprp2, qPlastic[j][i][t]);
						exprg1 = cplex.sum(exprg1, qGlass[i][j][t]);
						exprg2 = cplex.sum(exprg2, qGlass[j][i][t]);
					}
				}
				exprp1 = cplex.sum(exprp1, xPlastic[j][t]);
				exprg1 = cplex.sum(exprg1, xGlass[j][t]);
				cplex.addEq(exprp1, exprp2);
				cplex.addEq(exprg1, exprg2);
			}
		}

		// Constraint 12
		for (int t=1; t <= timeHorizon; t++) {
			IloNumExpr exprp1 = qPlastic[0][0][t];
			IloNumExpr exprg1 = qGlass[0][0][t];
			IloNumExpr exprp2 = cPlastic[t];
			IloNumExpr exprg2 = cGlass[t];
			for (int i=1; i < nodes; i++) {
				exprp1 = cplex.sum(exprp1, qPlastic[i][0][t]);
				exprg1 = cplex.sum(exprg1, qGlass[i][0][t]);
				exprp2 = cplex.sum(exprp2, xPlastic[i][t]);
				exprg2 = cplex.sum(exprg2, xGlass[i][t]);
			}
			cplex.addEq(exprp1, exprp2);
			cplex.addEq(exprg1, exprg2);
		}

		// Constraint 13
		for (int t=0; t <= timeHorizon; t++) {
			IloNumExpr exprp = cplex.constant(0.0);
			IloNumExpr exprg = cplex.constant(0.0);
			for (int i=0; i < nodes; i++) {
				exprp = cplex.sum(exprp, qPlastic[0][i][t]);
				exprg = cplex.sum(exprg, qGlass[0][i][t]);
			}
			cplex.addEq(exprp, cPlastic[t]);
			cplex.addEq(exprg, cGlass[t]);
		}

		// Constraint 14
		for (int t=1; t <= timeHorizon; t++) {
			for (int i=0; i < nodes; i++) {
				for (int j=0; j < nodes; j++) {
					if (i != j) {
						IloNumExpr exprp = cplex.prod(capacityTruck, yPlastic[i][j][t]);
						IloNumExpr exprg = cplex.prod(capacityTruck, yGlass[i][j][t]);
						cplex.addLe(qPlastic[i][j][t], exprp);
						cplex.addLe(qGlass[i][j][t], exprg);
					}
				}
			}
		}

		// Constraint 15
		for (int t=1; t <= timeHorizon; t++) {
			IloNumExpr sumyp = cplex.constant(0.0);
			IloNumExpr sumyg = cplex.constant(0.0);
			for (int j=1; j < nodes; j++) {
				sumyp = cplex.sum(sumyp, yPlastic[0][j][t]);
				sumyg = cplex.sum(sumyg, yGlass[0][j][t]);
			}
			IloNumExpr exprp = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, sumyp));
			IloNumExpr exprg = cplex.sum(capacityTruck, cplex.prod(-capacityTruck, sumyg));
			cplex.addLe(qPlastic[0][0][t], exprp);
			cplex.addLe(qGlass[0][0][t], exprg);
		}
	}
	
	public void addOverflowConstraint() throws IloException {
		// CONSTRAINT Overflow
		for(int t=1; t<=timeHorizon; t++) {
			for(int i=1; i<nodes; i++) {
				cplex.addLe(fPlastic[i][t],instance.getLocation(i).getPlasticContainer().getCapacity());
				cplex.addLe(fGlass[i][t],instance.getLocation(i).getGlassContainer().getCapacity());
			}
		}

	}
	
	public void addBeginingOfDayCapacityConstraint() throws IloException {
		for(int t=1; t<=timeHorizon; t++) {
			IloNumExpr expr19a = cplex.constant(0.0);
			expr19a = cplex.prod(capacityTruck,cplex.sum(1,cplex.prod(-1, etaPlastic[t])));
			cplex.addLe(cPlastic[t],expr19a);

			IloNumExpr expr19b = cplex.constant(0.0);
			expr19b = cplex.prod(capacityTruck,cplex.sum(1,cplex.prod(-1, etaGlass[t])));
			cplex.addLe(cGlass[t],expr19b);
		}

		for(int t=1; t<=timeHorizon; t++) {
			IloNumExpr expr20a = cplex.constant(0.0);
			IloNumExpr expr20b = cplex.constant(0.0);
			IloNumExpr expr21a = cplex.constant(0.0);
			IloNumExpr expr21b = cplex.constant(0.0);
			for(int i=0; i<nodes; i++) {
				expr20a = cplex.sum(qPlastic[i][0][t-1],expr20a);	
				expr20b = cplex.sum(qGlass[i][0][t-1],expr20b);
				expr21a = cplex.sum(qPlastic[i][0][t-1],expr21a);
				expr21b = cplex.sum(qGlass[i][0][t-1],expr21b);	
			}
			cplex.addLe(cPlastic[t],expr20a);
			cplex.addLe(cGlass[t],expr20b);
			expr21a = cplex.sum(cplex.prod(-capacityTruck, etaPlastic[t]),expr21a);
			//expr21a = cplex.sum(-0.1,expr21a);//@todo
			cplex.addGe(cPlastic[t],expr21a);
			expr21b = cplex.sum(cplex.prod(-capacityTruck, etaGlass[t]),expr21b);
			//expr21b = cplex.sum(-0.1,expr21a);//@todo
			cplex.addGe(cGlass[t],expr21b);
			
		}
		
		// CONSTRAINT One Recycling facility
		for(int t=1; t<=timeHorizon; t++) {
			IloNumExpr expr = cplex.constant(0.0);
			expr = cplex.sum(etaPlastic[t],etaGlass[t]);
			cplex.addLe(expr, 1);
		}
	}
	
	public void addSwapsConstraint() throws IloException {
		// CONSTRAINT Individual Swap 1
				for(int t=1; t<=timeHorizon; t++) {
					IloNumExpr expr1p1 = cplex.constant(0.0);
					expr1p1 = cplex.sum(zGlass[t],etaPlastic[t]);
					expr1p1 = cplex.sum(-1,expr1p1);
					cplex.addLe(expr1p1, p[0][t]);

					IloNumExpr expr2p1 = cplex.constant(0.0);
					expr2p1 = cplex.sum(zPlastic[t],etaGlass[t]);
					expr2p1 = cplex.sum(-1,expr2p1);
					cplex.addLe(expr2p1, p[0][t]);
				}

				// CONSTRAINT Individual Swap 2
				for(int t=1; t<=timeHorizon; t++) {
					IloNumExpr expr1p2 = cplex.constant(0.0);
					expr1p2 = cplex.sum(zGlass[t],expr1p2);
					for(int j=1; j<nodes; j++) {
						expr1p2 = cplex.sum(yPlastic[0][j][t],expr1p2);	
					}
					expr1p2 = cplex.sum(cplex.prod(-1, p[0][t]),expr1p2);
					expr1p2 = cplex.sum(-1,expr1p2);
					cplex.addLe(expr1p2, p[1][t]);

					IloNumExpr expr2p2 = cplex.constant(0.0);
					expr2p2 = cplex.sum(zPlastic[t],expr2p2);
					for(int j=1; j<nodes; j++) {
						expr2p2 = cplex.sum(yGlass[0][j][t],expr2p2);	
					}
					expr2p2 = cplex.sum(cplex.prod(-1, p[0][t]),expr2p2);
					expr2p2 = cplex.sum(-1,expr2p2);
					cplex.addLe(expr2p2, p[1][t]);

					IloNumExpr expr3p2 = cplex.constant(0.0);
					expr3p2 = cplex.sum(etaPlastic[t],expr3p2);
					for(int j=1; j<nodes; j++) {
						expr3p2 = cplex.sum(yGlass[0][j][t],expr3p2);	
					}
					expr3p2 = cplex.sum(-1,expr3p2);
					cplex.addLe(expr3p2, p[1][t]);

					IloNumExpr expr4p2 = cplex.constant(0.0);
					expr4p2 = cplex.sum(etaGlass[t],expr4p2);
					for(int j=1; j<nodes; j++) {
						expr4p2 = cplex.sum(yPlastic[0][j][t],expr4p2);	
					}
					expr4p2 = cplex.sum(-1,expr4p2);
					cplex.addLe(expr4p2, p[1][t]);
				}

				// CONSTRAINT Individual Swap 3
				for(int t=1; t<=timeHorizon-1; t++) {
					IloNumExpr expr1p3 = cplex.constant(0.0);
					expr1p3 = cplex.sum(zGlass[t],zPlastic[t+1]);
					expr1p3 = cplex.sum(cplex.prod(-1,p[0][t]),expr1p3);
					expr1p3 = cplex.sum(cplex.prod(-1,p[1][t]),expr1p3);
					expr1p3 = cplex.sum(-1,expr1p3);
					cplex.addLe(expr1p3, p[2][t]);

					IloNumExpr expr2p3 = cplex.constant(0.0);
					expr2p3 = cplex.sum(zPlastic[t],zGlass[t+1]);
					expr2p3 = cplex.sum(cplex.prod(-1,p[0][t]),expr2p3);
					expr2p3 = cplex.sum(cplex.prod(-1,p[1][t]),expr2p3);
					expr2p3 = cplex.sum(-1,expr2p3);
					cplex.addLe(expr1p3, p[2][t]);

					IloNumExpr expr3p3 = cplex.constant(0.0);
					expr3p3 = cplex.sum(etaPlastic[t],zGlass[t+1]);
					expr3p3 = cplex.sum(cplex.prod(-1,p[1][t]),expr3p3);
					expr3p3 = cplex.sum(-1,expr3p3);
					cplex.addLe(expr3p3, p[2][t]);

					IloNumExpr expr4p3 = cplex.constant(0.0);
					expr4p3 = cplex.sum(etaGlass[t],zPlastic[t+1]);
					expr4p3 = cplex.sum(cplex.prod(-1,p[1][t]),expr4p3);
					expr4p3 = cplex.sum(-1,expr4p3);
					cplex.addLe(expr4p3, p[2][t]);

					IloNumExpr expr5p3 = cplex.constant(0.0);
					for(int j=1; j<nodes; j++) {
						expr5p3 = cplex.sum(yGlass[0][j][t],expr5p3);	
					}
					expr5p3 = cplex.sum(zPlastic[t+1],expr5p3);
					expr5p3 = cplex.sum(-1,expr5p3);
					cplex.addLe(expr5p3, p[2][t]);

					IloNumExpr expr6p3 = cplex.constant(0.0);
					for(int j=1; j<nodes; j++) {
						expr6p3 = cplex.sum(yPlastic[0][j][t],expr6p3);	
					}
					expr6p3 = cplex.sum(zGlass[t+1],expr6p3);
					expr6p3 = cplex.sum(-1,expr6p3);
					cplex.addLe(expr6p3, p[2][t]);
				}

				// CONSTRAINT Total Swaps
				for(int t=1; t<=timeHorizon; t++) {
					IloNumExpr expr = cplex.constant(0.0);
					for(int k=0; k<3; k++) {
						expr = cplex.sum(p[k][t],expr);
					}
					cplex.addEq(expr, s[t]);
				}

				// CONSTRAINT Truck at the end of the day
				for(int t=1; t<=timeHorizon; t++) {
					IloNumExpr expr = cplex.constant(0.0);
					expr = cplex.sum(zPlastic[t], zGlass[t]);
					cplex.addEq(expr, 1);
				}
	}
	
	public void addMaxWorkingDayConstraint() throws IloException {
		// CONSTRAINT Working days
		for(int t=1; t<=timeHorizon; t++) {
			IloNumExpr expr = cplex.constant(0.0);
			expr = cplex.sum(cplex.prod(m, s[t]), expr);
			expr = cplex.sum(cplex.prod(recyclingPlastic, etaPlastic[t]), expr);
			expr = cplex.sum(cplex.prod(recyclingGlass, etaGlass[t]), expr);
			for(int i=0; i < nodes; i++) {
				for(int j=0; j < nodes; j++) {
					if (i != j) {
						expr = cplex.sum(cplex.prod(instance.getDistance(i, j), yPlastic[i][j][t]), expr);
						expr = cplex.sum(cplex.prod(instance.getDistance(i, j), yGlass[i][j][t]), expr);
						if(i != 0) {
							expr = cplex.sum(cplex.prod(instance.getLocation(i).getPlasticEmptyTime(),yPlastic[i][j][t]),expr);
							expr = cplex.sum(cplex.prod(instance.getLocation(i).getGlassEmptyTime(),yGlass[i][j][t]),expr);
						}
					}
				}
			}
			cplex.addLe(expr, 480);
		}
	}
	
	public void addObjective() throws IloException{
		IloNumExpr obj = cplex.constant(0.0);
		for (int t=1; t <= timeHorizon; t++) {
			obj = cplex.sum(cplex.prod(m, s[t]), obj);
			obj = cplex.sum(cplex.prod(recyclingPlastic, etaPlastic[t]), obj);
			obj = cplex.sum(cplex.prod(recyclingGlass, etaGlass[t]), obj);
			for(int i=0; i < nodes; i++) {
				for(int j=0; j < nodes; j++) {
					if (i != j) {
						obj = cplex.sum(cplex.prod(instance.getDistance(i, j), yPlastic[i][j][t]), obj);
						obj = cplex.sum(cplex.prod(instance.getDistance(i, j), yGlass[i][j][t]), obj);
						if (i != 0) {
							obj = cplex.sum(cplex.prod(instance.getLocation(i).getPlasticEmptyTime(), yPlastic[i][j][t]), obj);
							obj = cplex.sum(cplex.prod(instance.getLocation(i).getGlassEmptyTime(), yGlass[i][j][t]), obj);
						}
					}
				}
			}
		}
		cplex.addMinimize(obj);
	}

	public void solve() throws IloException {
		cplex.solve();
	}
	
	public double getObjective() throws IloException{
		return cplex.getObjValue();
	}
	

}
