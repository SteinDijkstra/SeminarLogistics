/**
 * Data structure that represents a Container keeps track of the actual amount of garbage as well
 * as the predicted amount. Also is defined by a maximum capicity, mean garbage disposed and the std
 * of the disposed garbage.
 * @author steindijkstra
 *
 */
public class Container {
	private final double capacity; //max capacity of this container
	private final double meanGarbageDisposed; //mean garbage disposed
	private final double stdGarbageDisposed; //standard deviation of garbage disposed
	private double actualAmountGarbage; //amount of garbage present in container
	private double predictedAmountGarbage; //amount of garbage for planning purposes
	
	//-------------------Constructors-----------------
	/**
	 * Constructor for a container with stochastic demand
	 * @param capacity nonnegative value that represents the maximum capacity of a container
	 * @param mean nonnegative integer that represent the mean amount of garbage that is disposed each day
	 * @param std nonnegative integer that represent the standard deviation of the amount of garbage
	 */
	public Container(double capacity, double mean, double std) {
		if(capacity<0||mean<0||std<0) {
			throw new IllegalArgumentException("Please enter nonnegative arguments for this Container");
		}
		this.capacity=capacity;
		meanGarbageDisposed=mean;
		stdGarbageDisposed=std;
	}
	
	/**
	 * Constructor for a container with deterministic demand
	 * @param capacity nonnegative value that represents the maximum capacity of a container
	 * @param mean nonnegative integer that represent the mean amount of garbage that is disposed each day
	 */
	public Container(double capacity, double mean) {
		if(capacity<0||mean<0) {
			throw new IllegalArgumentException("Please enter nonnegative arguments for this Container");
		}
		this.capacity=capacity;
		meanGarbageDisposed=mean;
		stdGarbageDisposed=0;
	}
	
	//--------------Setters and getters----------------
	/**
	 * Returns the actual amount of garbage present in the container
	 * DO NOT USE FOR PLANNING PURPOSES
	 * @return double value of cubic amount of garbage
	 */
	public double getActualAmountGarbage() {
		return actualAmountGarbage;
	}
	
	/**
	 * Changes the actual amount of garbage by the specified amount
	 * Change is added to the current amount. i.e. if the change is negative it is subtracted
	 * If the new value would lead to a negative amount of garbage an exception is thrown
	 * @param change double value that specifies the change
	 */
	public void changeActualAmountGarbage(double change) {
		if(actualAmountGarbage+change<0) {
			throw new IllegalArgumentException("amount of garbage can not be negative");
		} else {
			actualAmountGarbage+=change;
		}
	}
	
	/**
	 * Returns the predicted amount of garbage present in the container
	 * DO NOT USE FOR EMPTYING AND OVERFLOW STATISTIC PURPOSES
	 * @return double value of cubic amount of garbage
	 */
	public double getPredictedAmountGarbage() {
		return predictedAmountGarbage;
	}
	
	/**
	 * Changes the predicted amount of garbage by the specified amount
	 * Change is added to the current amount. i.e. if the change is negative it is subtracted
	 * If the new value would lead to a negative amount of garbage an exception is thrown
	 * @param change double value that specifies the change
	 */
	public void changePredictedAmountGarbage(double change) {
		if(predictedAmountGarbage+change<0) {
			throw new IllegalArgumentException("amount of garbage can not be negative");
		} else {
			predictedAmountGarbage+=change;
		}
	}
	
	/**
	 * Return capacity of container
	 * @return double in cubes
	 */
	public double getCapacity() {
		return capacity;
	}
	
	//--------------Utility methods--------------------
	/**
	 * Update predicted and actual amount of garbage. the randomNumber should be a based on a
	 * standard normal number s.t. the additional garbage is distributed by N(mean, std^2)
	 * @param randomNumber a double gotton from a N(0,1) variable.
	 */
	public void update(double randomNumber) {
		changePredictedAmountGarbage(meanGarbageDisposed);
		double change=meanGarbageDisposed+randomNumber*stdGarbageDisposed;
		changeActualAmountGarbage(change<0?change:0);
	}
	
	
	//--------------Other methods----------------------
	
	
	
}
