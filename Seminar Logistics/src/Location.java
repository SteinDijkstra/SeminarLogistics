/**
 * This class represents an AAP (aparte afvalpunt) and contains a plastic and glass container
 * that can have a different max capacities and other properties.   
 * @author steindijkstra
 *
 */
public class Location {
	private final Graph graph; //graph the location is part of
	private final int locationNumber; //index of the current location
	private Container glassContainer; 
	private Container plasticContainer;
	private final int glassEmptyTime;
	private final int plasticEmptyTime;
	//empty time glass
	//empty time plastic

	//-------------------Constructors-----------------
	/**
	 * Create a new Location
	 * @param graph which it is part of
	 * @param locationNumber index of this location
	 * @param glass a glass container
	 * @param plastic a plastic container
	 */
	public Location(Graph graph,int locationNumber, Container glass, Container plastic, int glassTime, int plasticTime) {
		this.graph=graph;
		this.locationNumber=locationNumber;
		this.glassContainer=glass;
		this.plasticContainer=plastic;
		this.glassEmptyTime=glassTime;
		this.plasticEmptyTime= plasticTime;
	}
	


	

	//--------------Setters and getters----------------
	/**
	 * Return time to empty plastic
	 * @return integer amount of time
	 */
	public int getPlasticEmptyTime() {
		return plasticEmptyTime;
	}
	
	/**
	 * Return time to empty glass
	 * @return integer amount of time
	 */
	public int getGlassEmpyTime() {
		return glassEmptyTime;
	}
	
	/**
	 * Returns predicted amount of glass
	 * @return double in cubes
	 */
	public double getPredictedGlass() {
		return glassContainer.getPredictedAmountGarbage();
	}
	
	/**
	 * Returns actual amount of glass
	 * @return double in cubes
	 */
	public double getActualGlass() {
		return glassContainer.getActualAmountGarbage();
	}
	
	/**
	 * Returns predicted amount of plastic
	 * @return double in cubes
	 */
	public double getPredictedPlastic() {
		return plasticContainer.getPredictedAmountGarbage();
	}
	
	/**
	 * Returns actual amount of plastic
	 * @return double in cubes
	 */
	public double getActualPlastic() {
		return plasticContainer.getActualAmountGarbage();
	}
	
	/**
	 * Returns the glass container
	 * @return a container object
	 */
	public Container getGlassContainer() {
		return glassContainer;
	}
	/**
	 * Returns the plastic container
	 * @return a container object
	 */
	public Container getPlasticContainer() {
		return plasticContainer;
	}
	
	/**
	 * Returns index
	 * @return Integer
	 */
	public int getIndex() {
		return locationNumber;
	}
	//--------------Utility methods--------------------
	
	
	
	//--------------Other methods----------------------
}
