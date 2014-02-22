package tetris.agent;

/** Generates trajectories up to a fixed length by running a policy. 
 * @author Humhu
 *
 */
public class FixedLengthTrajectoryGenerator extends TrajectoryGenerator {

	protected int _trajLength; // The length of trajectories to generate
	
	public FixedLengthTrajectoryGenerator(StateGenerator gen, Policy policy, 
			RewardFunction reward, int length) {
		super(gen, policy, reward);
		_trajLength = length;
	}
	
	public FixedLengthTrajectoryGenerator(FixedLengthTrajectoryGenerator other) {
		super(other);
		_trajLength = other._trajLength;
	}
	
	public Trajectory generate_trajectory() {
		
		initialize();
		for (int i = 0; i < _trajLength; i++) {
			step();
			if (_currentState.hasLost()) {
				break;
			}
		}
		return get_trajectory(); // This makes a deep copy
		
	}

	@Override
	public TrajectoryGenerator copy() {
		TrajectoryGenerator ret = new FixedLengthTrajectoryGenerator(this);
		return ret;
	}

}
