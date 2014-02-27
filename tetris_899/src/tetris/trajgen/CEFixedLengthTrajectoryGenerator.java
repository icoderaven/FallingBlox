package tetris.trajgen;

import tetris.agent.*;
import org.ejml.simple.SimpleMatrix;

/** Generates trajectories up to a fixed length by running a Cross-Entropy policy, and 
 * saves trajectory and weights.  Basically FixedLengthTrajectory + weights
 * @author eavrunin
 */

public class CEFixedLengthTrajectoryGenerator extends CETrajectoryGenerator {
	
	protected int _trajLength; // The length of trajectories to generate
	
	/**
	 * Create a FixedLengthTrajectory generator. Makes deep copies of arguments.
	 * @param gen - StateGenerator to copy.
	 * @param policy - Policy to copy.
	 * @param reward - RewardFunction to copy.
	 * @param length - Max length of trajectories to generate.
	 */
	public CEFixedLengthTrajectoryGenerator(StateGenerator gen, CEPolicy policy, 
			RewardFunction reward, int length) {
		super(gen, policy, reward);
		_trajLength = length;
	}
	
	/**
	 * Copy constructor.
	 * @param other - CEFixedLengthTrajectoryGenerator to copy.
	 */
	public CEFixedLengthTrajectoryGenerator(CEFixedLengthTrajectoryGenerator other) {
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
	public CETrajectoryGenerator copy() {
		CETrajectoryGenerator ret = new CEFixedLengthTrajectoryGenerator(this);
		return ret;
	}

}
