package tetris.trajgen;
import tetris.simulator.State;

public abstract class StateGenerator {

	// Return a deep copy of this object
	abstract StateGenerator copy();
	
	// Generate a state!
	abstract State generate_state();
	
}
