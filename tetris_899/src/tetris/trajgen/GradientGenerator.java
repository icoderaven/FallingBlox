package tetris.trajgen;

import java.util.concurrent.Callable;

import org.ejml.simple.SimpleMatrix;

import tetris.agent.GradientPolicy;
import tetris.agent.Trajectory;
import tetris.agent.Trajectory.SARTuple;

public class GradientGenerator implements Callable<GradientResult> {

	private GradientPolicy _policy;
	private Trajectory _traj;
	
	public GradientGenerator(GradientPolicy pi, Trajectory traj) {
		_policy = new GradientPolicy(pi);
		_traj = traj;
	}
	
	public GradientResult calculate_gradient() {
		
		double gamma = _policy.get_gamma();
		SimpleMatrix params = _policy.get_params();
		
		SimpleMatrix trajGradSum = new SimpleMatrix( params.numRows(), 1 );
		trajGradSum.set(1E-3);
		
		for(int t = 0; t < _traj.tuples.size(); t++)
		{
			SARTuple sar = _traj.tuples.get(t);
			SimpleMatrix grad = _policy.gradient(sar.state, sar.action);
			
			trajGradSum = trajGradSum.plus(grad);
			
		}
		trajGradSum = trajGradSum.scale( 1.0/_traj.tuples.size() );
		
		SimpleMatrix covs = trajGradSum.mult( trajGradSum.transpose() );
		GradientResult res = new GradientResult( trajGradSum, covs, _traj.tuples.size(), 
				_traj.sum_rewards_tail(0, gamma), 
				_traj.tuples.get( _traj.tuples.size() - 1 ).state.getRowsCleared() );
		return res;
	}
	
	public GradientResult call() {
		return calculate_gradient();
	}

}
