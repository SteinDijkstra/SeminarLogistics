import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExactSmall {
	private static Graph model; // TODO initialize right graph
	private static int optimalTime=Integer.MAX_VALUE;
	private static List<Integer> optimalRoute;
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		model= Utils.init();
		model.initGarbage();
		model.updateGarbage();
		List<Location> toVisit=model.getLocWithContainerOverBound(1, true);
		System.out.println("find optimal route through: "+toVisit);
		solve(toVisit,true);
		System.out.println("Optimal route is: "+getOptimalRoute());
		System.out.println("with time: "+getOptimalTime());
	}
	
	public static void solve(List<Location> toVisit,boolean isPlastic) {
		List<Integer>currentRoute=new ArrayList<>();
		currentRoute.add(0);
		int emptyTime=0;
		for(Location loc:toVisit) {
			if(isPlastic) {
				emptyTime+=loc.getPlasticEmptyTime();
			} else {
				emptyTime+=loc.getGlassEmpyTime();
			}
		}
		findRoute(toVisit,currentRoute, emptyTime);
	}
	
	public static void findRoute(List<Location>toVisit, List<Integer>currentRoute,int currentTime) {
		if(toVisit.isEmpty()) {
			int dist=model.getDistance(currentRoute.get(currentRoute.size()-1), 0);
			currentTime+=dist;
			currentRoute.add(0);
			System.out.println("New solution: "+currentRoute);
			System.out.println("with time: "+currentTime);
			if(currentTime<optimalTime) {
				optimalTime=currentTime;
				optimalRoute=new ArrayList<>(currentRoute);
			}
			currentTime-=dist;
			currentRoute.remove(currentRoute.size()-1);
			
		} else {
			for(int i=0;i<toVisit.size();i++) {
				Location newLoc=toVisit.remove(i);;
				int dist=model.getDistance(currentRoute.get(currentRoute.size()-1), newLoc.getIndex());
				currentTime+=dist;
				currentRoute.add(newLoc.getIndex());
				findRoute(toVisit,currentRoute,currentTime);
				currentTime-=dist;
				toVisit.add(i,newLoc);
				currentRoute.remove(currentRoute.size()-1);
			}
		}
	}
	
	public static int getOptimalTime() {
		return optimalTime;
	}
	public static List<Integer> getOptimalRoute(){
		return optimalRoute;
	}

}
