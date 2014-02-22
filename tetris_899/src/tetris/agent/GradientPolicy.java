package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

/** A Policy interface implementation for Policy Gradient method
 * @author icoderaven
 */

public class GradientPolicy implements Policy {

	public Feature _feature;
	private SimpleMatrix _params;

	public GradientPolicy(Feature featureGenerator, SimpleMatrix parameters) {
		_feature = featureGenerator;
		_params = parameters;
	}
	
	public GradientPolicy(GradientPolicy other) {
		_feature = other._feature.copy();
		_params = other._params;
	}
	
	// TODO HH - This is totally wrong. It needs to sample.
	@Override
	public Action get_action(SimState curr_state) {
		//Return the action with the highest probability
		SimpleMatrix dist = pi(curr_state);
		int index =0;
		double max = 0.0;
		for(int i=0;i<dist.numRows();i++)
		{
			double val = dist.get(i);
			if (max > val)
			{
				max = val;
				index = i;
			}
		}
		return new Action(index);
	}

	@Override
	public void fit_policy(Trajectory[] t) {
		//TODO Average gradient over trajectories
	}

	@Override
	public double pi(SimState s, Action a) {
		SimpleMatrix dist = pi(s);
		return dist.get(a.index);
	}
	
	@Override
	public SimpleMatrix pi(SimState s) {
		/// For current piece get all possible actions
		int[][] moves = s.legalMoves();
		SimpleMatrix probs = new SimpleMatrix(moves.length, 1);
		// Evaluate function for all actions
		for (int a_prime = 0; a_prime < probs.numRows(); a_prime++) {
			probs.set(a_prime, function_evaluator(s, new Action(a_prime)) );
		}
		return probs.divide(probs.elementSum());
	}

	public double function_evaluator(State s, Action a) {
		return Math.exp(_params.dot(_feature.get_feature_vector(s, a)));
	}

	@Override
	public SimpleMatrix gradient(SimState s, Action a) {
		SimpleMatrix J_for_a = _feature.get_feature_vector(s, a);
		SimpleMatrix pi_for_s = pi(s);
		//Each feature vector gets one column
		SimpleMatrix all_features = new SimpleMatrix(J_for_a.numRows(),pi_for_s.numRows());
		
		for (int a_prime = 0; a_prime < pi_for_s.numRows(); a_prime++) {
			//Insert the feature vector into the specified column
			all_features.insertIntoThis(0, a_prime, _feature.get_feature_vector(s, new Action(a_prime)) );
		}
		
		SimpleMatrix E_for_s = all_features.mult(pi_for_s);
		
		return J_for_a.minus(E_for_s);
	}

	@Override
	public Policy copy() {
		// TODO Auto-generated method stub
		return null;
	}



}
