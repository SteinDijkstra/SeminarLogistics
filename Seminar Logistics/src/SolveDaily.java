import java.io.IOException;
import java.util.List;

public class SolveDaily {
	public static boolean hasPlasticContainer=true;
	public static final double MAX_PLASTIC=20;
	public static final double MAX_GLASS=20;
	public static final int SWAP_TIME=20;
	public static final int GLASS_REC_TIME=130;
	public static final int PLASTIC_REC_TIME=130;
	private static Graph model;
	public static double amountGlass=0;
	public static double amountPlastic=0;
	
	public static void main(String[]args) throws NumberFormatException, IOException {
		Graph model=Utils.init();
		model.initGarbage();
		init(model);
		solve(0.95);
		System.out.println(model.getAmountOverflow());
		model.updateGarbage();
		solve(0.95);
		System.out.println(model.getAmountOverflow());
	}
	
	public static void init(Graph model) {
		SolveDaily.model=model;
		ExactSmall.init(model);
	}
	
	public static int solve(double percentage) {
		//obtain plastic Route
		List<Location> plasticToVisit=model.getLocWithContainerOverBound(percentage, true);
		ExactSmall.solve(plasticToVisit,true);
		List<Integer> plasticRoute=ExactSmall.getOptimalRoute();
		int maxPlasticTime=ExactSmall.getOptimalTime();
		//obtain plastic Route
		List<Location> glassToVisit=model.getLocWithContainerOverBound(percentage, true);
		ExactSmall.solve(glassToVisit,false);
		List<Integer> glassRoute=ExactSmall.getOptimalRoute();
		int maxGlassTime=ExactSmall.getOptimalTime();
		//check if containers should be emptied and if so which
		double newPlastic= getPredictedPlasticRoute(plasticRoute);
		double newGlass= getPredictedGlassRoute(glassRoute);
		boolean emptyGlass=false;
		boolean emptyPlastic=false;
		int emptyTime=0;
		if(amountGlass+newGlass>amountPlastic+newPlastic) {
			if(amountGlass+newGlass>MAX_GLASS) {
				emptyGlass=true;
				emptyTime=GLASS_REC_TIME;
			}
		} else {
			if(amountPlastic+newPlastic>MAX_PLASTIC) {
				emptyPlastic=true;
				emptyTime=PLASTIC_REC_TIME;
			}
		}
		//check number of swaps
		int swaps=0;
		if(emptyGlass&&hasPlasticContainer) {
			swaps++;
			hasPlasticContainer=!hasPlasticContainer;
		} else if(emptyPlastic&&!hasPlasticContainer) {
			swaps++;
			hasPlasticContainer=!hasPlasticContainer;
		}
		swaps++;
		hasPlasticContainer=!hasPlasticContainer;
		//check if route is feasible (less than 480) if not print that route is infeasible
		
		
		if(maxGlassTime+maxPlasticTime+swaps*SWAP_TIME+emptyTime>480) {
			System.out.println("Route is infeasible in better version change some things");
		}
		
		//perform route -> update garbage bins
		if(emptyGlass) {
			amountGlass=0;
		} else if(emptyPlastic) {
			amountPlastic=0;
		}
	
		int actualPlasticTime=performPlasticRoute(plasticRoute);
		int actualGlassTime=performGlassRoute(glassRoute);
		//return actual time spent during the day
		return actualGlassTime+actualPlasticTime+swaps*SWAP_TIME+emptyTime;
		
	}
	
	public static double getPredictedGlassRoute(List<Integer>route) {
		double result=0;
		for(Integer loc:route) {
			result+=model.getLocation(loc).getPredictedGlass();
		}
		return result;
	}
	
	public static double getPredictedPlasticRoute(List<Integer>route) {
		double result=0;
		for(Integer loc:route) {
			result+=model.getLocation(loc).getPredictedPlastic();
		}
		return result;
	}
	
	public static int performPlasticRoute(List<Integer>route) {
		int index=1;
		int time=0;
		while(index<route.size()-1&&amountPlastic<MAX_PLASTIC) {
			Location loc=model.getLocation(route.get(index));
			time+=model.getDistance(route.get(index-1), route.get(index));
			double amountCollected=loc.emptyPlastic(MAX_PLASTIC-amountPlastic);
			amountPlastic+=amountCollected;
			time+=loc.getPlasticEmptyTime();
			index++;
		}
		time+=model.getDistance(route.get(index-1), 0);
		return time;
	}
	
	public static int performGlassRoute(List<Integer>route) {
		int index=1;
		int time=0;
		while(index<route.size()-1&&amountGlass<MAX_GLASS) {
			Location loc=model.getLocation(route.get(index));
			time+=model.getDistance(route.get(index-1), route.get(index));
			double amountCollected=loc.emptyGlass(MAX_GLASS-amountGlass);
			amountGlass+=amountCollected;
			time+=loc.getGlassEmptyTime();
			index++;
		}
		time+=model.getDistance(route.get(index-1), 0);
		return time;
	}

}
