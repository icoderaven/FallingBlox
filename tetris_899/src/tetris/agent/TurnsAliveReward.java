package tetris.agent;

import tetris.simulator.State;

/**
 * Generates rewards based on the number of turns alive. The reward can
 * be weighted by a scalar.
 * @author Humhu
 *
 */
public class TurnsAliveReward implements RewardFunction {

	protected double _weight;
	
	public TurnsAliveReward() {
		_weight = 1.0;
	}
	
	public TurnsAliveReward(double weight) {
		_weight = weight;
	}
	
	@Override
	public double GetReward(State state, Action action) {
		return _weight*state.getTurnNumber();
	}

}
