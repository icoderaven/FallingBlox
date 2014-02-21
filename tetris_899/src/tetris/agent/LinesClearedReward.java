package tetris.agent;

import tetris.simulator.State;

/**
 * Returns rewards based on the number of lines cleared in the state. The reward
 * can be weighted by a scalar.
 * @author Humhu
 *
 */
public class LinesClearedReward implements RewardFunction {

	final protected double _weight;
	
	public LinesClearedReward() {
		_weight = 1.0;
	}
	
	public LinesClearedReward(double weight) {
		_weight = weight;
	}
	
	public double GetReward(State state, Action action) {
		return _weight*state.getRowsCleared();
	}

}
