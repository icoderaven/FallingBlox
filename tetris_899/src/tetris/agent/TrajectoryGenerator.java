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
		_stateGen = gen.copy();
		_policy = policy.copy();
		_rewardFunc = reward.copy();
	}
	
	public TrajectoryGenerator(TrajectoryGenerator other) {
		_stateGen = other._stateGen.copy();
		_policy = other._policy.copy();
		_rewardFunc = other._rewardFunc.copy();
	}
	
	// TODO Reduce boilerplate by using reflection
	// Return a deep copy of this object
	public abstract TrajectoryGenerator copy();
	
	// Generates a trajectory and returns it. This may return the same
	// trajectory in subsequent calls.
	abstract public Trajectory generate_trajectory();
	
	/** Initializes the current state to a starting state and resets the
	 * Trajectory record.
	 */
	protected void initialize() {
		if(_stateGen != null) {
			_currentState = _stateGen.generate_state();
		} else {
			
		}
		_trajectory._trajectory.clear();
	}
	
	// Applies the specified action to the trajectory
	protected void step() {
		
		// Record history and take a step
		Action action = _policy.get_action(_currentState);
		double reward = _rewardFunc.get_reward(_currentState, action);
		_trajectory.add(_currentState, action, reward);
		
		action.apply(_currentState); // This modifies _currentState!
	}
	
	protected Trajectory get_trajectory() {
		return new Trajectory(_trajectory);
	}
	
}
