package tetris.trajgen;

import tetris.agent.*;
import java.util.concurrent.Callable;
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
public abstract class TrajectoryGenerator implements Callable<Trajectory> {

	// These are our generators/evaluators
	protected Policy _policy;
	protected StateGenerator _stateGen;
	protected RewardFunction _rewardFunc;
	
	// Work variables for recording trajectories and such
	protected Trajectory _trajectory;
	protected State _currentState;
//	protected double lastReward;
	
	private State deadState;
	private State preDeadState;
	Action fatalAction;
	
	/**
	 * Construct a TrajectoryGenerator from a StateGenerator, Policy, and RewardFunction. This
	 * method makes copies of the arguments to use.
	 * @param gen - A StateGenerator to copy
	 * @param policy - A Policy to copy
	 * @param reward - A RewardFunction to copy
	 */
	public TrajectoryGenerator(StateGenerator gen, Policy policy, RewardFunction reward) {
		_stateGen = gen.copy();
		_policy = policy.copy();
		_rewardFunc = reward.copy();
	}
	
	/**
	 * Copy constructor
	 * @param other - TrajectoryGenerator to copy
	 */
	public TrajectoryGenerator(TrajectoryGenerator other) {
		_stateGen = other._stateGen.copy();
		_policy = other._policy.copy();
		_rewardFunc = other._rewardFunc.copy();
	}
	
	/**
	 * Create a deep copy of this generator.
	 * @return A deep copy of this generator.
	 */
	public abstract TrajectoryGenerator copy();
	
	/**
	 * Returns a generated Trajectory
	 */
	public Trajectory call() {
		return get_trajectory();
	}
	
	/**
	 * Generate a trajectory using the respective policy starting from a state produced
	 * by the state generator with rewards from the reward function.
	 * @return A deep copy of the generated trajectory.
	 */
	public Trajectory get_trajectory() {
		initialize();
		generate_trajectory();
		return new Trajectory(_trajectory);
	}
	
	/**
	 * Initialize the generator by resetting internal fields and generating a new 
	 * starting state.
	 */
	protected void initialize() {
		_currentState = _stateGen.generate_state();
//		lastReward = 0;
		_trajectory = new Trajectory();
	}
	
	/**
	 * Execute the policy for one step.
	 */
	protected void step() {
		
		// Record history and take a step
		int action = _policy.get_action(_currentState);
		
//		System.out.format("Making an action %d%n", action.index);
		
		double reward = _rewardFunc.get_reward(_currentState, action);
	
//		State stateCopy = new State(_currentState); // Not needed because SARTuple makes a copy
		_trajectory.add(_currentState, action, reward);
		
		// This accounts for odd lag in our reward functions
//		lastReward = reward;
		
		_currentState.makeMove(action); // This modifies _currentState!
		
//		if(_currentState.hasLost()) {
//			System.out.format("Died in step on turn %d%n", _currentState.getTurnNumber());
//		} else {
//			System.out.format("Still alive and kicking on turn %d%n", _currentState.getTurnNumber());
//		}
		
	}
	
	/**
	 * Produce a local trajectory and save it in the local _trajectory.
	 */
	abstract protected void generate_trajectory();
	
}
