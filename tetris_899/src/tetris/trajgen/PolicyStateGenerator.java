package tetris.trajgen;

import tetris.agent.Action;
import tetris.agent.Policy;
import tetris.simulator.State;

public class PolicyStateGenerator extends StateGenerator {

	protected Policy _policy;
	protected State _startState;
	protected int _horizon;
	
	public PolicyStateGenerator(Policy policy, State startState, int numSteps) {
		_policy = policy.copy();
		_startState = new State(startState);
		_horizon = numSteps;
	}
	
	public PolicyStateGenerator(PolicyStateGenerator other) {
		_policy = other._policy.copy();
		_startState = new State(other._startState);
		_horizon = other._horizon;
	}
	
	StateGenerator copy() {
		StateGenerator ret = new PolicyStateGenerator(this);
		return ret;
	}

	State generate_state() {
		State ret = new State(_startState);
		for(int i = 0; i < _horizon; i++) {
			int a = _policy.get_action(ret);
			ret.makeMove(a);
		}
		return ret;
	}

}
