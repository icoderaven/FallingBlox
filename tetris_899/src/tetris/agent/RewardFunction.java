package tetris.agent;
import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;

/** Classes implementing this interface calculate rewards from state-action pairs.
 *  In general, functions estimating action-values, values, or whatever will implement
 *  this interface. 
 *  
 * @author Humhu
 *
 */
public abstract class RewardFunction {

	// A scaling factor applied to all outputs
	final protected double _weight;
	
	// Default constructor with unit weight
	public RewardFunction() {
		_weight = 1.0;
	}
	
	// Constructor with a specified weight
	public RewardFunction(double weight) {
		_weight = weight;
	}
	
	// A copy constructor
	public RewardFunction(RewardFunction other) {
		this(other._weight);
	}
	
	// Make a deep copy of this object
	public abstract RewardFunction copy();
	
	// Sort of?
	public double estimate_value(State state, Policy policy) {
		double sum = 0.0;
		SimpleMatrix probs = policy.pi(state);
		for(int a_prime = 0; a_prime < probs.numRows(); a_prime++) {
			sum += probs.get(a_prime)*get_reward(state, a_prime);
		}
		return sum;
	}
	
	/** 
	 * Calculate the scaled reward associated with a state-action pair.
	 * @param state - The state to calculate the reward for.
	 * @param action - The action to calculate the reward for.
	 * @return The reward associated with R(state, action)
	 */
	public double get_reward(State state, int action) {
		return _weight*calculate_reward(state, action);
	}
	
	/**
	 * Calculates the reward associated with a state-action pair.
	 * @param state - The state to calculate the reward for.
	 * @param action - The action to calculate the reward for.
	 * @return The reward associated with R(state, action)
	 */
	abstract protected double calculate_reward(State state, int action);
	
}
