package tetris.agent;
import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

public interface Policy {
	
	// Return a deep copy of this object
	public Policy copy();
	
	//Method to get an action given state
	public Action get_action(State curr_state);
	
	//Method to fit a policy given state_{t} action_{t} reward tuples
	public void fit_policy(Trajectory[] t);

	//Method to calculate the prob of action given a state action tuple
	public double pi(State s, Action a);
	
	//Method to calculate the prob. distribution of action given state
	public SimpleMatrix pi(State s);
	
	//Method to return the gradient wrt parameter
	public SimpleMatrix gradient(State s, Action a);
}
