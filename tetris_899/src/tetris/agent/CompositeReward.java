package tetris.agent;

import tetris.simulator.State;

public class CompositeReward extends RewardFunction {

	protected RewardFunction _comp1 = null, _comp2 = null;
	
	public CompositeReward(RewardFunction funcA, RewardFunction funcB) {
		_comp1 = funcA.copy();
		_comp2 = funcB.copy();
	}
	
	public CompositeReward(CompositeReward other) {
		_comp1 = other._comp1.copy();
		_comp2 = other._comp2.copy();
	}
	
	@Override
	public RewardFunction copy() {
		RewardFunction ret = new CompositeReward(this);
		return ret;
	}

	@Override
	protected double calculate_reward(State state, Action action) {
		return _comp1.calculate_reward(state, action) + _comp2.calculate_reward(state, action);
	}

}
