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
	
	@Override
	public Action get_action(State curr_state) {
		//Obtain the distribution of probabilities for the current action 
		SimpleMatrix dist = pi(curr_state);
		
		int index = dist.numRows()-1;
		//Get a random number sampled from the uniform distribution  
		double u = Math.random(), cdf = 0.0;
		//Move till the running total exceeds this probability value
		for(int i=0; i<dist.numRows(); i++){
			cdf+=dist.get(i);
			if (u <= cdf){
				index = i;
				break;
			}
		}
		return new Action(index);
	}

	@Override
	public void fit_policy(Trajectory[] t_list) {
		//TODO Average gradient over trajectories
		//Create a container for all the evaluated deltas
		SimpleMatrix deltas = new SimpleMatrix(_params.numRows(), t_list.length);
		
		SimpleMatrix delta = new SimpleMatrix(_params.numRows(), 1);
		SimpleMatrix z = new SimpleMatrix(t_list[0].tuples.get(0).state.legalMoves().length, 1);
		Trajectory t;
		double gamma = 0.1; //Constant for hysteresis of z
		for(int i=0; i<t_list.length; i++)
		{
			delta.set(0);
			z.set(0);
			t = t_list[i];
			//Move through every tuple in this trajectory, maintaining running averages
			for(int j=0; j<t.tuples.size(); j++)
			{
				//z_{t+1} = gamma*z_{t} + gradient(s,a)
				z = z.scale(gamma).plus(gradient(t.tuples.get(j).state, t.tuples.get(j).action));
				//delta_{t+1} = delta + (1/t+1)(r_{t+1}*z_{t+1} - delta)
				delta = delta.plus(1.0/(j+1), z.scale(t.tuples.get(j).reward).minus(delta));
			}
			//Add this to the corresponding column of the big container matrix
			deltas.insertIntoThis(0, i, delta);
		}
		//Alright, now average these deltas together into our final delta
		//Interesting idea - use the covariance of these vectors to determine how much we step along this gradient
		SimpleMatrix mean_delta = new SimpleMatrix(_params.numRows(),1);
		for(int i=0; i<deltas.numRows(); i++)
		{
			mean_delta.set(i, deltas.extractVector(true, i).elementSum());
		}
		mean_delta.scale(1.0/deltas.numCols());
		
		//Step params in this direction
		//TODO Figure out how to be smarter about the step
		double step = 1.0;
		_params.plus(step, mean_delta);
	}

	@Override
	public double pi(State s, Action a) {
		SimpleMatrix dist = pi(s);
		return dist.get(a.index);
	}
	
	@Override
	public SimpleMatrix pi(State s) {
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
	public SimpleMatrix gradient(State s, Action a) {
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
		return new GradientPolicy(this);
	}


}
