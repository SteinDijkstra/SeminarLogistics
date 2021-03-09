/**
 * Data structure that represents a Container keeps track of the actual amount of garbage as well
 * as the predicted amount. Also is defined by a maximum capicity, mean garbage disposed and the std
 * of the disposed garbage.
 * @author steindijkstra
 *
 */
public class Container {
	private final int capacity; //max capacity of this container
	private final int meanGarbageDisposed; //mean garbage disposed
	private final int stdGarbageDisposed; //standard deviation of garbage disposed
	private double actualAmountGarbage; //amount of garbage present in container
	private double predictedAmountGarbage; //amount of garbage for planning purposes
	
	//-------------------Constructors-----------------
	/**
	 * Constructor for a container with stochastic demand
	 * @param capacity nonnegative value that represents the maximum capacity of a container
	 * @param mean nonnegative integer that represent the mean amount of garbage that is disposed each day
	 * @param std nonnegative integer that represent the standard deviation of the amount of garbage
	 */
	public Container(int capacity, int mean, int std) {
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
	public Container(int capacity, int mean) {
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
	 * if more garbage is added than the capacity the value is set equal to the capacity
	 * @param change double value that specifies the change
	 */
	public void changeActualAmountGarbage(double change) {
		if(actualAmountGarbage+change<0) {
			throw new IllegalArgumentException("amount of garbage can not be negative");
		} else if(actualAmountGarbage+change>capacity) {
			//TODO more than capacity is allowed
			actualAmountGarbage=capacity;
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
	 * if more garbage is added than the capacity the value is set equal to the capacity
	 * @param change double value that specifies the change
	 */
	public void changePredictedAmountGarbage(double change) {
		if(predictedAmountGarbage+change<0) {
			throw new IllegalArgumentException("amount of garbage can not be negative");
		} else if(predictedAmountGarbage+change>capacity) {
			predictedAmountGarbage=capacity;
		} else {
			predictedAmountGarbage+=change;
		}
	}
	
	//--------------Utility methods--------------------
	/**
	 * Update predictedn and actual amount of garbage. the randomNumber should be a based on a
	 * standard normal number s.t. the additional garbage is distributed by N(mean, std^2)
	 * @param randomNumber a double gotton from a N(0,1) variable.
	 */
	public void update(double randomNumber) {
		predictedAmountGarbage+=meanGarbageDisposed;
		// TODO check what should happen if addition is negative
		actualAmountGarbage+=meanGarbageDisposed+randomNumber*stdGarbageDisposed;
	}
	
	
	//--------------Other methods----------------------
	
	
	
}
