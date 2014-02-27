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
		return new PolicyStateGenerator(this);
	}

	State generate_state() {
		State ret = new State(_startState);
		for (int i = 0; i < _horizon; i++) {
			Action a = _policy.get_action(ret);
			a.apply(ret);
		}
		State clone = new State(ret);
		clone.makeMove(_policy.get_action(clone).index);
		if (clone.hasLost()) {
			System.out.format("%n%naaaaaaaaaaaaaaaaaaaaaaaah%n");
			return generate_state();
		} else {
			return ret;
		}

	}

}
