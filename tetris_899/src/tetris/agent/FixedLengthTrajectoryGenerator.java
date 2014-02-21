package tetris.agent;
import tetris.simulator.State;

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
	
	public Trajectory GenerateTrajectory() {
		
		Initialize();
		for (int i = 0; i < _trajLength; i++) {
			Step();
			if (_currentState.hasLost()) {
				break;
			}
		}
		return GetTrajectory(); // This makes a deep copy
		
	}

}
