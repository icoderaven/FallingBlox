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
		SimpleMatrix z = new SimpleMatrix( params.numRows(), 1 );
		
		SimpleMatrix delta = new SimpleMatrix( params.numRows(), 1 );
		SimpleMatrix covs = new SimpleMatrix( params.numRows(), params.numRows() );
		
		z.set(0);
		delta.set(0);
		covs.set(0);
		
		for(int t = 0; t < _traj.tuples.size(); t++)
		{
			SARTuple sar = _traj.tuples.get(t);
			SimpleMatrix grad = _policy.gradient(sar.state, sar.action);
			
			// z_t+1 = gamma*z_t + grad
			z = z.scale(gamma).plus( grad );
			
			// delta_t+1 = delta_t*(t/(t+1)) + r_t+1 * z_t+1 / t+1
			delta = delta.scale( 1.0*t/( t + 1.0 ) ).plus( z.scale( sar.reward/( t + 1.0 ) ) );
			
			covs = covs.plus( grad.mult( grad.transpose() ) ); 
			
		}
		
		delta = delta.plus( delta.scale( 1.0/_traj.tuples.size() ) );
		covs = covs.plus( covs.scale( 1.0/_traj.tuples.size() ) );
		
		GradientResult res = new GradientResult( delta, covs, _traj.tuples.size(), 
				_traj.sum_rewards_tail(0, gamma), 
				_traj.tuples.get( _traj.tuples.size() - 1 ).state.getRowsCleared() );
		return res;
	}
	
	public GradientResult call() {
		return calculate_gradient();
	}

}
