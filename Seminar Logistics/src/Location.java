/**
 * This class represents an AAP (aparte afvalpunt) and contains a plastic and glass container
 * that can have a different max capacities and other properties.   
 * @author Stein
 *
 */
public class Location {
	private final Graph graph; //graph the location is part of
	private final int locationNumber; //index of the current location
	private Container glassContainer; 
	private Container plasticContainer;
	private final int glassEmptyTime;
	private final int plasticEmptyTime;

	//-------------------Constructors-----------------
	/**
	 * Create a new Location
	 * @param graph which it is part of
	 * @param locationNumber index of this location
	 * @param glass a glass container
	 * @param plastic a plastic container
	 */
	public Location(Graph graph,int locationNumber, Container glass, Container plastic, int glassTime, int plasticTime) {
		this.graph = graph;
		this.locationNumber = locationNumber;
		this.glassContainer = glass;
		this.plasticContainer = plastic;
		this.glassEmptyTime = glassTime;
		this.plasticEmptyTime = plasticTime;
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
	public int getGlassEmptyTime() {
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
	/** DO NOT USE FOR PLANNING PURPOSES
	 * check if plastic is overflowing
	 * @return true if current amount is more than the capacity
	 */
	public boolean plasticOverflow() {
		return plasticContainer.isOverflow();
	}

	/** DO NOT USE FOR PLANNING PURPOSES
	 * Check if glass is overflowing
	 * @return true if current amount is more than the capacity
	 */
	public boolean glassOverflow() {
		return glassContainer.isOverflow();
	}
	
	/**
	 * Chceck if either plastic or glass flows over
	 * @return returns 1 if flows over, 0 otherwise
	 */
	public int isOverflow() {
		int result = 0;
		if (plasticOverflow() || glassOverflow())
			result = 1;
		return result;
	}

	/**
	 * Return the distance to all neighbors
	 * @return integer array with distances
	 */
	public int[] getDistanceNeighbours() {
		return graph.getDistanceNeighbours(this.locationNumber);
	}

	/**
	 * Return a double array with the predicted plastic all neighbors have
	 * @return a double array with plastic amount of neighbors
	 */
	public double[] getPlasticAmountNeighbours() {
		double[] result = new double[graph.getNumLocations()];
		int index = 0;
		for(Location neighbour : graph.getLocations()) {
			result[index] = neighbour.getPredictedPlastic();
			index++;
		}
		return result;
	}
	/**
	 * return a double array with the predicted glass all neighbors have
	 * @return a double array with glass amounf of neighbors in cubes
	 */
	public double[] getGlassAmountNeighbours() {
		double[] result = new double[graph.getNumLocations()];
		int index = 0;
		for(Location neighbour:graph.getLocations()) {
			result[index] = neighbour.getPredictedGlass();
			index++;
		}
		return result;
	}

	/**
	 * Empty the plastic container where the max additional capacity can be specified
	 * @param maxEmpty leftover capacity of the truck
	 * @return the actual amount emptied
	 */
	public double emptyPlastic(double maxEmpty) {
		if(plasticContainer.getActualAmountGarbage() > maxEmpty) {//not all garbage can be collected
			plasticContainer.changeActualAmountGarbage(-maxEmpty);
			plasticContainer.setPredictedAmountGarbage(plasticContainer.getActualAmountGarbage());//TODO how accurate can this be estimated
			return maxEmpty;
		} else { //all garbage can be collected
			double amountEmptied=plasticContainer.getActualAmountGarbage();
			plasticContainer.setActualAmountGarbage(0);
			plasticContainer.setPredictedAmountGarbage(0);
			return amountEmptied;
		}
	}

	/**
	 * Empty the glass container where the max additional capacities specified
	 * @param maxEmpty leftover capacity of the truck
	 * @return the actual amount emptied
	 */
	//TODO: Check if all garbage is collected, also outside container in case of overflow.
	public double emptyGlass(double maxEmpty) {
		if(glassContainer.getActualAmountGarbage() > maxEmpty) {//not all garbage can be collected
			glassContainer.changeActualAmountGarbage(-maxEmpty);
			glassContainer.setPredictedAmountGarbage(glassContainer.getActualAmountGarbage());//TODO how accurate can this be estimated
			return maxEmpty;
		} else { //all garbaged can be collected
			double amountEmptied = glassContainer.getActualAmountGarbage();
			glassContainer.setActualAmountGarbage(0);
			glassContainer.setPredictedAmountGarbage(0);
			return amountEmptied;
		}
	}
		
	//--------------Other methods----------------------
	@Override
	public String toString() {
		String description = "Location "+this.locationNumber+"\n Plastic: "+plasticContainer.toString();
		description += "\n Glass: "+glassContainer.toString();
		return description;
	}
}
