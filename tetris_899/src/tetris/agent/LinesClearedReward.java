package tetris.agent;

import tetris.simulator.State;

/**
 * Returns rewards based on the number of lines cleared in the state. The reward
 * can be weighted by a scalar.
 * @author Humhu
 *
 */
public class LinesClearedReward extends RewardFunction {
	
	// Default constructor with unit weight
	public LinesClearedReward() {}
	
	// Constructor with specified weight
	public LinesClearedReward(double weight) {
		super(weight);
	}
	
	// Copy constructor
	public LinesClearedReward(LinesClearedReward other) {
		super(other);
	}
	
	/**
	 * Returns a reward based on the number of rows cleared so far in the history
	 * of state.
	 */
	public double calculate_reward(State state, Action action) {
		return _weight*state.getRowsCleared();
	}

	public RewardFunction copy() {
		RewardFunction ret = new LinesClearedReward(this);
		return ret;
	}

}
