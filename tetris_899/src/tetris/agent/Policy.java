package tetris.agent;
import tetris.simulator.*;

public interface Policy {
	
	//Method to get an action given state
	public int get_action(State curr_state);
	
	//Method to fit a policy given state_{t} action_{t} state_{t+1} tuples
	public void fit_policy(State s_t[], Action a_t[], State s_tplus[]);

	//Method to calculate the prob of action given a state action tuple
	public double pi(SimState s, Action a);
	
	//Method to return the gradient wrt parameter
	public double[] gradient(State s, Action a);
}
