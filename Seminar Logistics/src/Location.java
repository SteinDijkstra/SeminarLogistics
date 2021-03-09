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
	 * 
	 * @param graph
	 * @param locationNumber
	 * @param glass
	 * @param plastic
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
	
	
	
	
	//--------------Utility methods--------------------
	
	
	
	//--------------Other methods----------------------
}
