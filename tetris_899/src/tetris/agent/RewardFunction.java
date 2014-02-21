package tetris.agent;
import tetris.simulator.State;

/** Classes implementing this interface calculate rewards from state-action pairs.
 *  In general, functions estimating action-values, values, or whatever will implement
 *  this interface. 
 *  
 * @author Humhu
 *
 */
public interface RewardFunction {

	/** Calculate the reward associated with a state-action pair.
	 * 
	 * @param state - The state to calculate the reward for.
	 * @param action - The action to calculate the reward for.
	 * @return The reward associated with R(state, action)
	 */
	double GetReward(State state, Action action);
	
}
