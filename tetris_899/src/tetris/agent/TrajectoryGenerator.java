package tetris.agent;
import tetris.simulator.State;

/** 
 * These classes generate trajectories from policies in a variety 
 * of ways. The initial state for the trajectories are drawn from a 
 * given StateGenerator, actions are determined using a given Policy,
 * and rewards are calculated using a given RewardFunction.
 * 
 * @see Policy
 * @see StateGenerator
 * @see RewardFunction
 * 
 * These classes are not in general thread-safe and should be wrapped
 * by another class implementing the Runnable interface with protection.
 * @author Humhu
 *
 */
public abstract class TrajectoryGenerator {

	// These are our generators/evaluators
	protected Policy _policy;
	protected StateGenerator _stateGen;
	protected RewardFunction _rewardFunc;
	
	// Work variables for recording trajectories and such
	protected Trajectory _trajectory;
	protected State _currentState;
	
	public TrajectoryGenerator(StateGenerator gen, Policy policy, RewardFunction reward) {

		_stateGen = gen;
		_policy = policy;
		_rewardFunc = reward;
	}
	
	// Generates a trajectory and returns it. This may return the same
	// trajectory in subsequent calls.
	abstract public Trajectory GenerateTrajectory();
	
	/** Initializes the current state to a starting state and resets the
	 * Trajectory record.
	 */
	protected void Initialize() {
		if(_stateGen != null) {
			_currentState = _stateGen.GenerateState();
		} else {
			
		}
		_trajectory._trajectory.clear();
	}
	
	// Applies the specified action to the trajectory
	protected void Step() {
		
		// Record history and take a step
		Action action = _policy.get_action(_currentState);
		double reward = _rewardFunc.GetReward(_currentState, action);
		_trajectory.Add(_currentState, action, reward);
		
		action.Apply(_currentState); // This modifies _currentState!
	}
	
	protected Trajectory GetTrajectory() {
		return new Trajectory(_trajectory);
	}
	
}
