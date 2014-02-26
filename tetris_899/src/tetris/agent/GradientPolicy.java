package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

/** A Policy interface implementation for Policy Gradient method
 * @author icoderaven
 */

public class GradientPolicy implements Policy {

	public Feature _feature;
	private SimpleMatrix _params;
	
	public GradientPolicy()
	{
		_feature = new BoardFeature();
		//_feature = new DefaultFeature();
		_params = new SimpleMatrix(_feature.get_feature_dimension(),1);
		_params.set(0.0);
		
//		for(int i=0;i < _params.numRows(); i++)
//		{
//			_params.set(i,Math.random());
//		}
//	
//		double[][] initParams = {{-40, -1, -80, -40, -40, 0, -1, 50}};
//		for(int i = 0; i < initParams[0].length; i++) {
//			_params.set(_params.numRows() - initParams[0].length + i, 
//					initParams[0][i]);
//		}
	}

	public GradientPolicy(Feature featureGenerator, SimpleMatrix parameters) {
		_feature = featureGenerator;
		_params = parameters;
	}
	
	public GradientPolicy(GradientPolicy other) {
		_feature = other._feature.copy();
		_params = new SimpleMatrix(other._params);
		_params = new SimpleMatrix(other._params);
	}
	
	public SimpleMatrix get_params() {
		return new SimpleMatrix(_params);
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
//			System.out.format("CDF: %f%n", cdf);
			if (u <= cdf){
				index = i;
				break;
			}
		}
		
		return new Action(index);
		
	}

	public void fit_policy(Trajectory[] t_list) {
		fit_policy(t_list, 1.0);
	}
	
	public void fit_policy(Trajectory[] t_list, double step_size) {
		//TODO Average gradient over trajectories
		//Create a container for all the evaluated deltas
		SimpleMatrix deltas = new SimpleMatrix(_params.numRows(), t_list.length);
		
		SimpleMatrix delta = new SimpleMatrix(_params.numRows(), 1);
		SimpleMatrix z = new SimpleMatrix(_params.numRows(), 1);
		double gamma = 0.99; //Constant for hysteresis of z
		
		SimpleMatrix gradCovs = new SimpleMatrix(_params.numRows(), _params.numRows());
		gradCovs.set(0);
		
		for(int i=0; i<t_list.length; i++)
		{
			delta.set(0);
			z.set(0);
			Trajectory t = t_list[i];
			//Move through every tuple in this trajectory, maintaining running averages
			double averageReward = t.sum_rewards(0, 1.0)/t.tuples.size();
			SimpleMatrix gradCovSum = new SimpleMatrix(_params.numRows(), _params.numRows());
			gradCovSum.set(0);		
			
//			System.out.format("Trajectory %d%n", i);
			
			for(int j=0; j<t.tuples.size(); j++)
			{
				//z_{t+1} = gamma*z_{t} + gradient(s,a)
				SimpleMatrix grad = gradient(t.tuples.get(j).state, t.tuples.get(j).action);
				
				gradCovSum = gradCovSum.plus( grad.mult( grad.transpose() ) );
				
				// Instantaneous reward, non-discounted
//				delta = delta.plus( grad.scale( t.tuples.get(j).reward) );
//				System.out.format("Reward at step %d: %f%n", j, t.tuples.get(j).reward);
				
				// Average reward, non-discounted
//				delta = delta.plus( grad.scale( averageReward ));
				
				// Reward to go, discounted
				delta = delta.plus( grad.scale( t.sum_rewards(j+1, gamma) - averageReward ) );
				
				// Running average reward, discounted
//				z = z.scale(gamma).plus(grad);
				//delta_{t+1} = delta + (1/t+1)(r_{t+1}*z_{t+1} - delta)
//				delta = delta.plus(1.0/(j+2), z.scale(t.sum_rewards(j+1, 1.0)).minus(delta));
//				System.out.println("Delta running average:");
//				delta.transpose().print();
				
			}
		

			SimpleMatrix gradCov = gradCovSum.scale(1.0/t.tuples.size());
			gradCovs = gradCovs.plus(gradCov);
			
			//Add this to the corresponding column of the big container matrix
			deltas.insertIntoThis(0, i, delta);
//			delta.transpose().print();
		}
		
		// Calculate average deltas
		SimpleMatrix mean_delta = new SimpleMatrix(_params.numRows(),1);
		for(int i=0; i<deltas.numRows(); i++)
		{
			mean_delta.set(i, deltas.extractVector(true, i).elementSum());
		}
		mean_delta = mean_delta.scale(1.0/deltas.numCols());
		
		// Calculate information matrix
		gradCovs = gradCovs.scale(1.0/t_list.length);
		gradCovs = gradCovs.plus( SimpleMatrix.identity(gradCovs.numRows()).scale(1.0) ); // Hack smoothing
		SimpleMatrix gradInfo = gradCovs.invert();
		
		// Scale delta by info matrix
		mean_delta = gradInfo.mult(mean_delta);
		
		//Step params in this direction
		double step = step_size;
		
		_params = _params.plus(step, mean_delta);
		System.out.format("Step size: %f%n", mean_delta.normF());
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
		SimpleMatrix exponents = new SimpleMatrix(moves.length, 1);
		SimpleMatrix probs = new SimpleMatrix(moves.length, 1);

		// Get all exponents first
		double maxVal = Double.NEGATIVE_INFINITY;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			double logLikelihood = calculate_log_likelihood(s, new Action(a_prime));
			if(logLikelihood > maxVal) {
				maxVal = logLikelihood;
			}
			exponents.set(a_prime, logLikelihood);
		}
//		double filteredMean = 0.0;
//		int meanCounter = 0;
//		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
//			double logLikelihood = calculate_log_likelihood(s, new Action(a_prime));
//			if(logLikelihood > -10) {
//				filteredMean += logLikelihood;
//				meanCounter++;
//			}
//			exponents.set(a_prime, logLikelihood);
//		}
//		filteredMean = filteredMean/meanCounter;
		
		double z = 0;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			double normalizedExponent = exponents.get(a_prime) - maxVal;
			double likelihood = Math.exp(normalizedExponent);
			z += likelihood;
			probs.set(a_prime, likelihood);
		}
		
		// Catch instances where exponent underflows and returns all 0
		if(z == 0) {
			System.out.println("PDF Underflow!");
			probs.set(1.0);
			z = probs.numRows();
		}
		
		// Normalize and add exploration
		probs = probs.divide(z);
		SimpleMatrix smoother = new SimpleMatrix(moves.length, 1);
		smoother.set(0.015/moves.length);
		probs = probs.plus(smoother);
		probs = probs.divide( probs.elementSum() );
		
		return probs;
	}

	protected double calculate_log_likelihood(State s, Action a) {
		SimpleMatrix temp = _feature.get_feature_vector(s,a);
		return _params.dot(temp);
	}
	
	public double function_evaluator(State s, Action a) {
		return Math.exp(calculate_log_likelihood(s, a));
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
		
//		System.out.format("nHoles a: %f%n", J_for_a.get(J_for_a.numRows() - 7));
//		all_features.extractVector(true, all_features.numRows() - 7).print();
		
		
		SimpleMatrix E_for_s = all_features.mult(pi_for_s);
		
//		System.out.format("nHoles E: %f%n", E_for_s.get(E_for_s.numRows() - 7));
		
//		all_features.print();
		return J_for_a.minus(E_for_s);
	}

//	protected void normalize_params() {
//		
//		double maxVal = Double.NEGATIVE_INFINITY;
//		
////		System.out.println("Params:");
////		_params.transpose().print();
//		for(int i = 0; i < _params.numRows(); i++) {
//			double val = _params.get(i);
//			if(val > maxVal) {
//				maxVal = val;
//			}
//		}
////		System.out.format("Max val: %f%n", maxVal);
//		for(int i = 0; i < _normalizedParams.numRows(); i++) {
//			_normalizedParams.set(i, _params.get(i) - maxVal + 1.0);
//		}
////		System.out.println("Normalized Params:");
////		_normalizedParams.transpose().print();
//	}
	
	@Override
	public Policy copy() {
		return new GradientPolicy(this);
	}

}
