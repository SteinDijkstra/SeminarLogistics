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
	public Container() {
		capacity=0;
		meanGarbageDisposed=0;
		stdGarbageDisposed=0;
	}
}
