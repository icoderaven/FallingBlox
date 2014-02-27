package tetris.agent;

import tetris.simulator.State;

public class DeathReward extends RewardFunction {
	
	public DeathReward(DeathReward other) {
		super(other);
	}
	
	public DeathReward(double d) {
		super(d);
	}

	@Override
	public RewardFunction copy() {
		// TODO Auto-generated method stub
		return new DeathReward(this);
	}

	@Override
	protected double calculate_reward(State state, Action action) {
		State copyState = new State(state);
		copyState.makeMove(action.index);
		return (copyState.hasLost()) ? 1.0 : 0.0;
	}

}
