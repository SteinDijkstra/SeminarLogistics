/**
 * An implementation of the traveling salesman problem in Java using dynamic 
 * programming to improve the time complexity from O(n!) to O(n^2 * 2^n).
 *
 * Time Complexity: O(n^2 * 2^n)
 * Space Complexity: O(n * 2^n)
 *
 **/

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ExactSmallDynamicProgramming {

	private int N, start;
	private int[][] distance;
	private List<Integer> tour;// = new ArrayList<>();
	private int minTourCost = Integer.MAX_VALUE;
	private boolean ranSolver = false;
	private Graph graph;
	private Map<Integer,Integer> original;
	private int collectionTime;

//	public static void main(String[] args) throws NumberFormatException, IOException {
//		ExactSmallDynamicProgramming routeModel= new ExactSmallDynamicProgramming(Utils.init());
//		List<Integer> toVisit= new ArrayList<>();
//		toVisit.add(1);toVisit.add(5);toVisit.add(7);
//		toVisit.add(112);toVisit.add(64);toVisit.add(13);
//		toVisit.add(21);toVisit.add(200);toVisit.add(187);
//		toVisit.add(22);toVisit.add(44);toVisit.add(33);
//		toVisit.add(71);toVisit.add(66);toVisit.add(153);
//		//toVisit.add(91);toVisit.add(151);toVisit.add(203);
//		//toVisit.add(206);toVisit.add(139);toVisit.add(172);
//		routeModel.solve(toVisit,false);
//		System.out.println(routeModel.getTour());
//		System.out.println(routeModel.getTourCost());
//
//
//	}

	public void solve(List<Integer> locToVisit,boolean isPlastic) {
		tour=new ArrayList<>();
		N=locToVisit.size()+1;
		System.out.println(N-1);
		this.start = 0;
		//Create map to decypher result
		original=new HashMap<>();
		original.put(0, 0);
		for(int i=0;i<locToVisit.size();i++) {
			original.put(i+1, locToVisit.get(i));
		}
		//Create distance matrix
		distance=new int[N][N];
		distance[0][0]=0;
		for(int i=0;i<locToVisit.size();i++) {
			distance[0][i+1]=distance[i+1][0]=graph.getDistance(0, locToVisit.get(i));
		}
		for(int i=0;i<locToVisit.size();i++) {
			for(int j=i;j<locToVisit.size();j++) {
				distance[i+1][j+1]=distance[j+1][i+1]=graph.getDistance(locToVisit.get(j), locToVisit.get(i));
			}
		}
		collectionTime=0;
		//determine collection Time
		for(int i=0;i<locToVisit.size();i++) {
			if(isPlastic) {
				collectionTime+=graph.getLocation(locToVisit.get(i)).getPlasticEmptyTime();
			} else {
				collectionTime+=graph.getLocation(locToVisit.get(i)).getGlassEmptyTime();
			}
		}
		solve();
		System.out.println(getTour());

	} 

	public ExactSmallDynamicProgramming(Graph graph) {
		this.graph=graph;
	}


	// Returns the optimal tour for the traveling salesman problem.
	public List<Integer> getTour() {
		//if (!ranSolver) solve();
		List<Integer>result=new ArrayList<>();
		for(Integer element:tour) {
			result.add(original.get(element));
		}

		return result;
	}

	// Returns the minimal tour cost.
	public int getTourCost() {
		//if (!ranSolver) solve();
		return minTourCost+collectionTime;
	}

	// Solves the traveling salesman problem and caches solution.
	public void solve() {

		

		final int END_STATE = (1 << N) - 1;
		Integer[][] memo = new Integer[N][1 << N];

		// Add all outgoing edges from the starting node to memo table.
		for (int end = 0; end < N; end++) {
			if (end == start) continue;
			memo[end][(1 << start) | (1 << end)] = distance[start][end];
		}

		for (int r = 3; r <= N; r++) {
			for (int subset : combinations(r, N)) {
				if (notIn(start, subset)) continue;
				for (int next = 0; next < N; next++) {
					if (next == start || notIn(next, subset)) continue;
					int subsetWithoutNext = subset ^ (1 << next);
					int minDist = Integer.MAX_VALUE;
					for (int end = 0; end < N; end++) {
						if (end == start || end == next || notIn(end, subset)) continue;
						int newDistance = memo[end][subsetWithoutNext] + distance[end][next];
						if (newDistance < minDist) {
							minDist = newDistance;
						}
					}
					memo[next][subset] = minDist;
				}
			}
		}

		// Connect tour back to starting node and minimize cost.
		for (int i = 0; i < N; i++) {
			if (i == start) continue;
			int tourCost = memo[i][END_STATE] + distance[i][start];
			if (tourCost < minTourCost) {
				minTourCost = tourCost;
			}
		}

		int lastIndex = start;
		int state = END_STATE;
		tour.add(start);

		// Reconstruct TSP path from memo table.
		for (int i = 1; i < N; i++) {

			int index = -1;
			for (int j = 0; j < N; j++) {
				if (j == start || notIn(j, state)) continue;
				if (index == -1) index = j;
				int prevDist = memo[index][state] + distance[index][lastIndex];
				int newDist  = memo[j][state] + distance[j][lastIndex];
				if (newDist < prevDist) {
					index = j;
				}
			}

			tour.add(index);
			state = state ^ (1 << index);
			lastIndex = index;
		}

		tour.add(start);
		Collections.reverse(tour);

		ranSolver = true;
	}

	private static boolean notIn(int elem, int subset) {
		return ((1 << elem) & subset) == 0;
	}

	// This method generates all bit sets of size n where r bits 
	// are set to one. The result is returned as a list of integer masks.
	public static List<Integer> combinations(int r, int n) {
		List<Integer> subsets = new ArrayList<>();
		combinations(0, 0, r, n, subsets);
		return subsets;
	}

	// To find all the combinations of size r we need to recurse until we have
	// selected r elements (aka r = 0), otherwise if r != 0 then we still need to select
	// an element which is found after the position of our last selected element
	private static void combinations(int set, int at, int r, int n, List<Integer> subsets) {

		// Return early if there are more elements left to select than what is available.
		int elementsLeftToPick = n - at;
		if (elementsLeftToPick < r) return;

		// We selected 'r' elements so we found a valid subset!
		if (r == 0) {
			subsets.add(set);
		} else {
			for (int i = at; i < n; i++) {
				// Try including this element
				set |= 1 << i;

				combinations(set, i + 1, r - 1, n, subsets);

				// Backtrack and try the instance where we did not include this element
				set &= ~(1 << i);
			}
		}
	}


}
