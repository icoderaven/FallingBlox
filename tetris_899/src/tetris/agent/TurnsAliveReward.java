package tetris.agent;

import tetris.simulator.State;

/**
 * Generates rewards based on the number of turns alive. The reward can
 * be weighted by a scalar.
 * @author Humhu
 *
 */
public class TurnsAliveReward extends RewardFunction {
	
	// Default constructor with unit weight
	public TurnsAliveReward() {}
	
	// Constructor with specified weight
	public TurnsAliveReward(double weight) {
		super(weight);
	}
	
	// Copy constructor
	public TurnsAliveReward(TurnsAliveReward other) {
		super(other);
	}
	
	/**
	 * Calculates the reward based on the turn number of the state
	 */
	protected double calculate_reward(State state, Action action) {
		State copyState = new State(state);
		action.apply(copyState);
		return (copyState.hasLost()) ? 0.0 : 1.0;
	}

	// Returns a deep copy
	public RewardFunction copy() {
		RewardFunction ret = new TurnsAliveReward(this);
		return ret;
	}

}
