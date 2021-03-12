import java.io.IOException;
import java.util.List;

public class Main {

	public static void main(String[] args) throws NumberFormatException, IOException {
		Graph model= Utils.init();
		model.initGarbage();
		int prevDay=0;
		for(int i=0;i<30;i++) {
			System.out.println("on day "+i+" we have "+(model.getAmountOverflow()-prevDay)+"new overflows");
			prevDay=model.getAmountOverflow();
			model.updateGarbage();
			
		}
		
	
	}
	


}
