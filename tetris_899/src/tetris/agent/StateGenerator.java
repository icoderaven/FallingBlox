package tetris.agent;
import tetris.simulator.State;

public interface StateGenerator {

	// Generate a state!
	abstract State GenerateState();
	
}
