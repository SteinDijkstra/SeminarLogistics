/**
 * This class represents an AAP (aparte afvalpunt) and contains a plastic and glass container
 * that can have a different max capacities and other properties.   
 * @author steindijkstra
 *
 */
public class Location {
	private final Graph graph; //graph the location is part of
	private final int index; //index of the current location
	private Container glassContainer; 
	private Container plasticContainer;
	
	public Location() {
		graph=null;
		index=0;
	}
	
}
