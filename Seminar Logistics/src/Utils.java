import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

public class Utils {
	public static Graph init() {
		
		
		return null;
	}
	public static Graph init(String glassDeposit, String plasticDeposit, String specialLoc, String travelTime) {
		return null;
		
		
	}
	
	public static int[][] readTravelTime(String travelTime) throws NumberFormatException, IOException {
		try(BufferedReader scan=new BufferedReader(new FileReader(new File(travelTime) ))){
			int nNodes= Integer.parseInt(scan.readLine());
			int[][] result = new int[nNodes][nNodes];
			
			String newLine;
			int from=0;
			while((newLine=scan.readLine())!=null) {
				String[] asciiNumbers=newLine.split(";");
				for(int to=0;to<asciiNumbers.length;to++) {
					result[from][to]=Integer.parseInt(asciiNumbers[to]); 
				}
				from++;
			}
			return result;
		}
	}
}
