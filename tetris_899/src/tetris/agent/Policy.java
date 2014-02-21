package tetris.agent;
import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

public interface Policy {
	
	//Method to get an action given state
	public Action get_action(SimState curr_state);
	
	//Method to fit a policy given state_{t} action_{t} reward tuples
	public void fit_policy(Trajectory[] t);

	//Method to calculate the prob of action given a state action tuple
	public double pi(SimState s, Action a);
	
	//Method to calculate the prob. distribution of action given state
	public SimpleMatrix pi(SimState s);
	
	//Method to return the gradient wrt parameter
	public SimpleMatrix gradient(SimState s, Action a);
}
