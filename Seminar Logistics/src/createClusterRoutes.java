import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class createClusterRoutes {
	private static final int maxDistanceRoute=420;
	public static void main(String[] args) throws IOException {
		List<List<Integer>> cluster= readClusters("clusters.csv");
		int[][] distances=Utils.readTravelTime("updated2_travel_time_matrix.csv");
		System.out.print(cluster.get(0));
	}
	public static List<List<Integer>> readClusters(String clusters) throws IOException {
		try(BufferedReader scan = new BufferedReader(new FileReader(new File(clusters) ))){
			int nClusters = Integer.parseInt(scan.readLine());
			List<List<Integer>>result= new ArrayList<>();

			String newLine;
			while((newLine = scan.readLine())!=null) {
				String[] asciiNumbers=newLine.split(";");
				List<Integer> cluster= new ArrayList<>();
				for(int i = 0; i < asciiNumbers.length; i++) {
					cluster.add( Integer.parseInt(asciiNumbers[i])); 
				}
				result.add(cluster);
			}
			return result;
		}
	}
	
	public static List<List<Integer>> allCombination(List<Integer>cluster, int distance){
		List<List<Integer>> result= new ArrayList<>();
		if(distance>maxDistanceRoute) {
			return result;
		}
		if(cluster.isEmpty()) {
			result.add(new ArrayList<>());
			return result;
		}
		
	}
	
	
	/*
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	        sets.add(new HashSet<T>());
	        return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	        Set<T> newSet = new HashSet<T>();
	        newSet.add(head);
	        newSet.addAll(set);
	        sets.add(newSet);
	        sets.add(set);
	    }       
	    return sets;
	}*/  
}
