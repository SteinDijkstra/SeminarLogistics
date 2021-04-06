import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class createClusterRoutes {
	//private static final int maxDistanceRoute=420;
	public static void main(String[] args) throws IOException {

		Graph model= Utils.init();
		List<List<Integer>>route=createSubsets(readClusters("cluster10.csv"),3);
		writeRoutes(route,model,"cluster10.3");
		//List<List<Integer>>routes=Utils.readRoutes("allRoutescluster10.4.csv");
//		Set<Integer>test= new HashSet<>();
//		for(List<Integer>route:routes) {
//			for(Integer loc:route) {
//				if(!test.contains(loc)) {
//					test.add(loc);
//				} else {
//					System.out.print("loc double is: "+loc);
//				}
//			}
//		}
		
		
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
	public static List<List<Integer>> createBasicRoutes(Graph model) {
		int nRoutes=model.getLocations().size();
		List<List<Integer>>result=new ArrayList<>();
		for(int i=1;i<model.getLocations().size();i++) {
			List<Integer>newRoute=new ArrayList<>();
			newRoute.add(i);
			result.add(newRoute);
		}
		return result;
	}
	public static void writeRoutes(List<List<Integer>>routes, Graph model,String name) throws IOException {
		List<Integer>distancesPlastic=new ArrayList<>();
		List<Integer>distancesGlass=new ArrayList<>();
		ExactSmall.setModel(model);
		for(List<Integer> route:routes) {
			List<Location>locRoute=new ArrayList<>();
			for(Integer locNumber:route) {
				locRoute.add(model.getLocation(locNumber));
			}
			ExactSmall.solve(locRoute, true);
			distancesPlastic.add(ExactSmall.getOptimalTime());
			ExactSmall.solve(locRoute, false);
			distancesGlass.add(ExactSmall.getOptimalTime());
			
		}
		writeFileDoubleList(routes,"allRoutes"+name+".csv");
		writeFileList(distancesPlastic,"allDistancesPlastic"+name+".csv");
		writeFileList(distancesPlastic,"allDistancesGlass"+name+".csv");
	}
	public static void writeFileDoubleList(List<List<Integer>> data,String filename) throws IOException {
		try(BufferedWriter br=new BufferedWriter(new FileWriter(new File(filename)))){
			br.write(""+data.size());
			br.newLine();
			int j=0;
			for(List<Integer>list:data) {
				j++;
				if(list.size()==0) {
					System.out.println("error");
				}
				br.write(""+list.get(0));

				for(int i=1;i<list.size();i++) {
					br.write(";"+list.get(i));
				}
				br.newLine();
			}
		}
	}
	public static void writeFileList(List<Integer> list,String filename) throws IOException {
		try(BufferedWriter br=new BufferedWriter(new FileWriter(new File(filename)))){
			br.write(""+list.size());
			br.newLine();
			for(Integer element:list) {
				br.write(""+element);
				br.newLine();
			}
		}
	}
	public static List<List<Integer>>  createSubsets(List<List<Integer>> clusters, int maxCardinality){
		List<List<Integer>>  subSets = new ArrayList<List<Integer>>();

		for(int i=0; i<clusters.size(); i++) {
			System.out.println("cluster: "+i);
				subSets.addAll(CreatePermutations(new ArrayList<Integer>(), clusters.get(i), new ArrayList<List<Integer>>(), maxCardinality));
		}
		return subSets;
	}

	public static List<List<Integer>> CreatePermutations(List<Integer> fixed, List<Integer> nonFixed, List<List<Integer>> resultsList, int maxCardinality) {
		System.out.print(".");
		//Checking whether there are no more elements to choose from, since there is only one or none.
		if((fixed.size() <= maxCardinality && nonFixed.isEmpty()) ||(fixed.size() == maxCardinality)) {
			//Creating the permutation
			
			//Adding the permutation to the list with permutations
			Collections.sort(fixed);
			if(!resultsList.contains(fixed)) {
				if(fixed.size()>0) {
					resultsList.add(fixed);
				}
			}	


		} else {
			//Looping through all the elements which should still be included.
			for(int i = 0; i < nonFixed.size(); i++) {

				//Creating the new lists with elements
				List<Integer>  newFixed = new ArrayList<>();
				List<Integer>  newNonFixed = new ArrayList<>();

				//Adding the already chosen elements to the new list.
				if(fixed!=null) {
					for (int j = 0; j < fixed.size(); j++) {
						newFixed.add(fixed.get(j));
					}
				}

				//Adding the new variable to the new list.
				newFixed.add(nonFixed.get(i));

				//Adding the elements which are still not chosen to the list.
				for (int j = 0; j < nonFixed.size(); j++) {
					if(j!=i) {
						newNonFixed.add(nonFixed.get(j));
					}
				}

				//Choosing the next element
				resultsList = CreatePermutations(newFixed, newNonFixed, resultsList, maxCardinality);
				nonFixed.remove(0);
				resultsList = CreatePermutations(fixed, nonFixed, resultsList, maxCardinality);	
			}

		}
		//Returning the list with permutations.
		return resultsList;
	}

}
