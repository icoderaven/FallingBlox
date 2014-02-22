package tetris.trajgen;

import tetris.simulator.State;

/**
 * Returns deep copies of a fixed state.
 * @author Humhu
 *
 */
public class FixedStateGenerator extends StateGenerator {

	// The fixed state
	protected State _state;
	
	// Default constructor
	public FixedStateGenerator(State s) {
		_state = new State(s);
	}
	
	// Deep copy constructor
	public FixedStateGenerator(FixedStateGenerator other) {
		_state = new State(other._state);
	}
	
	// Returns a deep copy in superclass form
	StateGenerator copy() {
		StateGenerator ret = new FixedStateGenerator(this);
		return ret;
	}

	// Returns a deep copy of the fixed state
	State generate_state() {
		return new State(_state);
	}

}
