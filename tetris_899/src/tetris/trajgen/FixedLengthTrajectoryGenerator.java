package tetris.trajgen;

import tetris.agent.*;

/** Generates trajectories up to a fixed length by running a policy. 
 * @author Humhu
 *
 */
public class FixedLengthTrajectoryGenerator extends TrajectoryGenerator {

	protected int _trajLength; // The length of trajectories to generate
	
	/**
	 * Create a FixedLengthTrajectory generator. Makes deep copies of arguments.
	 * @param gen - StateGenerator to copy.
	 * @param policy - Policy to copy.
	 * @param reward - RewardFunction to copy.
	 * @param length - Max length of trajectories to generate.
	 */
	public FixedLengthTrajectoryGenerator(StateGenerator gen, Policy policy, 
			RewardFunction reward, int length) {
		super(gen, policy, reward);
		_trajLength = length;
	}
	
	/**
	 * Copy constructor.
	 * @param other - FixedLengthTrajectoryGenerator to copy.
	 */
	public FixedLengthTrajectoryGenerator(FixedLengthTrajectoryGenerator other) {
		super(other);
		_trajLength = other._trajLength;
	}
	
	/**
	 * Fills _trajectory with a generated trajectory up to max length _trajLength.
	 */
	public void generate_trajectory() {
		
		for (int i = 0; i < _trajLength; i++) {
			step();
			if (_currentState.hasLost()) {
				break;
			}
		}
		
	}

	/**
	 * Produces a deep copy of this trajectory generator.
	 */
	public TrajectoryGenerator copy() {
		TrajectoryGenerator ret = new FixedLengthTrajectoryGenerator(this);
		return ret;
	}

}
