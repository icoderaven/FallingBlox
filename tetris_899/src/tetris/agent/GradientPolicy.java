package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.agent.Trajectory.SARTuple;
import tetris.simulator.*;

/** A Policy interface implementation for Policy Gradient method
 * @author icoderaven
 */

public class GradientPolicy implements Policy {

	public Feature _feature;
	private SimpleMatrix _params;
	private double _temperature;
	private double _gamma;
	private double _beta;
	
	public GradientPolicy()
	{
		_feature = new BoardFeature();
		//_feature = new DefaultFeature();
		_params = new SimpleMatrix(_feature.get_feature_dimension(),1);
		_params.set(0.0);
	
		_temperature = 1.0;
		_gamma = 0.95;
		_beta = 0.01;

	}

	public GradientPolicy(Feature featureGenerator, SimpleMatrix parameters) {
		_feature = featureGenerator;
		_params = parameters;
		_temperature = 1.0;
		_gamma = 0.95;
		_beta = 0.01;
	}
	
	public GradientPolicy(GradientPolicy other) {
		_feature = other._feature.copy();
		_params = new SimpleMatrix(other._params);
		_temperature = other._temperature;
		_gamma = other._gamma;
		_beta = other._beta;
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
	
	private SimpleMatrix calculate_baselines(Trajectory[] t_list) {
		
		SimpleMatrix baselines = new SimpleMatrix(_feature.get_feature_dimension(), 1);
		
		// Denominator and numerator accumulators over trajectory
		SimpleMatrix numerAcc = new SimpleMatrix(_feature.get_feature_dimension(), 1);
		SimpleMatrix denomAcc = new SimpleMatrix(_feature.get_feature_dimension(), 1);
		
		numerAcc.set(0);
		denomAcc.set(0);
		
		SimpleMatrix trajGradSum = new SimpleMatrix(_feature.get_feature_dimension(), 1);
		
		for(int i = 0; i < t_list.length; i++) {
			trajGradSum.set(0);
			
			for(int j = 0; j < t_list[i].tuples.size(); j++) {
				
				SARTuple sar = t_list[i].tuples.get(j);
				SimpleMatrix grad = gradient(sar.state, sar.action);
				
				// Update the sum of all gradients in this trajectory so far
				trajGradSum = trajGradSum.plus( grad );
				
			}
			
			// Calculate element-wise square of sum
			SimpleMatrix trajGradSumSquared = trajGradSum.elementMult( trajGradSum );
			double discountedReward = t_list[i].sum_rewards_tail(0, _gamma);
			
			// Averaged over trajectories
			numerAcc = numerAcc.plus( trajGradSumSquared.scale(discountedReward) );
			denomAcc = denomAcc.plus( trajGradSumSquared );
		}
		
		numerAcc = numerAcc.scale( 1.0/t_list.length );
		denomAcc = denomAcc.scale( 1.0/t_list.length );
		
		for(int i = 0; i < _feature.get_feature_dimension(); i++) {
			double bi = numerAcc.get(i)/(denomAcc.get(i) + 1E-6);
			baselines.set(i, bi);
		}
		return baselines;
		
	}
	
//	private SimpleMatrix calculate_baselines(Trajectory[] t_list) {
//
//		// First find the longest trajectory so we know how many baselines to calculate
//		int maxLength = 0;
//		for(int i = 0; i < t_list.length; i++) {
//			if(t_list[i].tuples.size() > maxLength) {
//				maxLength = t_list[i].tuples.size();
//			}
//		}
//		
//		// One baseline value per gradient dimension per timestep
//		SimpleMatrix baselines = new SimpleMatrix(_feature.get_feature_dimension(), maxLength);
//		SimpleMatrix gradSums = new SimpleMatrix(_feature.get_feature_dimension(), t_list.length);
//		double discount = 1.0;
//		
//		gradSums.set(0);
//		
//		// Denominator and numerator accumulators over trajectory
//		SimpleMatrix numerAcc = new SimpleMatrix(_feature.get_feature_dimension(), 1);
//		SimpleMatrix denomAcc = new SimpleMatrix(_feature.get_feature_dimension(), 1);
//		for(int i = 0; i < maxLength; i++) {
//			numerAcc.set(0);
//			denomAcc.set(0);
//			
//			for(int j = 0; j < t_list.length; j++) {
//				
//				if(i >= t_list[j].tuples.size()) { continue; } // Trajectory contributes nothing
//				
//				SARTuple sar = t_list[j].tuples.get(i);
//				SimpleMatrix grad = gradient(sar.state, sar.action);
//				
//				// Update the sum of all gradients in this trajectory so far
//				SimpleMatrix trajGradSum = gradSums.extractVector(false, j).plus(grad);
//				gradSums.insertIntoThis(0, j, trajGradSum);
//				
//				// Calculate element-wise square of sum
//				SimpleMatrix trajGradSumSquared = trajGradSum.elementMult( trajGradSum );
//				
//				// Add to the baseline estimate accumulator for this time step
//				denomAcc = denomAcc.plus( trajGradSumSquared );
//				numerAcc = numerAcc.plus( trajGradSumSquared.scale( discount*sar.reward ) );
//				
//			}
//			
//			// Averaged over trajectories
//			numerAcc = numerAcc.scale( 1.0/t_list.length );
//			denomAcc = denomAcc.scale( 1.0/t_list.length );
//			for(int j = 0; j < _feature.get_feature_dimension(); j++) {
//				double bji = numerAcc.get(j)/(denomAcc.get(j) + 1E-6);
//				baselines.set(j, i, bji );
//			}
//			
//			discount = discount*_gamma;
//			
//		}
//		
//		return baselines;
//		
//	}
	
	public void fit_policy(Trajectory[] t_list, double step_size) {
		SimpleMatrix gradSum = new SimpleMatrix(_params.numRows(), 1);
		SimpleMatrix trajGradSum = new SimpleMatrix(_params.numRows(), 1);
		SimpleMatrix gradCovs = new SimpleMatrix(_params.numRows(), _params.numRows());
		
		gradCovs.set(0);
		
		// Calculate optimal baselines
//		SimpleMatrix baselines = calculate_baselines(t_list);
//		baselines.transpose().print();
		
		int contrib = 0;
		for(int i=0; i<t_list.length; i++)
		{
			trajGradSum.set(0);
			
			double discount = 1.0;
			for(int j=0; j < t_list[i].tuples.size(); j++)
			{
				SARTuple sar = t_list[i].tuples.get(j);
				SimpleMatrix grad = gradient(sar.state, sar.action);
				
//				System.out.format("Step %d gradient:%n", j);
//				grad.transpose().print();
				
				trajGradSum = trajGradSum.plus(grad);
				
				SimpleMatrix discountedReward = new SimpleMatrix(_params.numRows(), 1);
				discountedReward.set( sar.reward );

//				System.out.format("Step %f reward:%n", sar.reward );
				
//				SimpleMatrix advantage = discountedReward.minus( baselines );
				SimpleMatrix advantage = discountedReward;
				
				SimpleMatrix gradContribution = trajGradSum.elementMult( advantage );
//				System.out.format("Step %d grad contribution:%n", j);
//				gradContribution.transpose().print();
				
				trajGradSum = trajGradSum.scale(_gamma);
				gradSum = gradSum.plus( gradContribution );
				
				// Keep a running total of the outer products for covariance calculation later
				gradCovs = gradCovs.plus( grad.mult( grad.transpose() ) );
				
				// Instantaneous reward, non-discounted
//				delta = delta.plus( grad.scale( t.tuples.get(j).reward) );
//				System.out.format("Reward at step %d: %f%n", j, t.tuples.get(j).reward);
				
				// Average reward, non-discounted
//				delta = delta.plus( grad.scale( averageReward ));
				
				// Reward to go, discounted
//				delta = delta.plus( grad.scale( t.sum_rewards_tail(j, _gamma) ) );
				
				// Running average reward, discounted
//				z = z.scale(gamma).plus(grad);
				//delta_{t+1} = delta + (1/t+1)(r_{t+1}*z_{t+1} - delta)
//				delta = delta.plus(1.0/(j+2), z.scale(t.sum_rewards(j, 1.0)).minus(delta));
//				System.out.println("Delta running average:");
//				delta.transpose().print();
				
				contrib++;
				discount = discount * _gamma;
			}

		}
		
		// Averaged over each time in each trajectory
		gradSum = gradSum.scale(1.0/contrib);
		gradCovs = gradCovs.scale(1.0/contrib);
		
		// Calculate information matrix
		gradCovs = gradCovs.plus( SimpleMatrix.identity(gradCovs.numRows()).scale(1E-3) ); // Hack smoothing
//		for(int i = 0; i < gradCovs.numRows(); i++) {
//			if(gradCovs.get(i,i) < 1E-3) {
//				gradCovs.set(i, i, 1.0);
//			}
//		}
		SimpleMatrix gradInfo = gradCovs.invert();
		
		// Scale delta by info matrix
		SimpleMatrix grad = gradInfo.mult(gradSum);
//		SimpleMatrix grad = gradSum;
		
		//Step params in this direction
		double step = step_size;
		_params = _params.plus(step, grad);
//		normalize_params();
		
		System.out.format("Step size: %f%n", Math.sqrt(grad.normF()) );
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
		boolean[] isFatal = new boolean[moves.length]; 
		SimpleMatrix exponents = new SimpleMatrix(moves.length, 1);
		SimpleMatrix probs = new SimpleMatrix(moves.length, 1);

		// Get all exponents first
		double maxVal = Double.NEGATIVE_INFINITY;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			
			double logLikelihood = calculate_log_likelihood(s, new Action(a_prime));
			isFatal[a_prime] = logLikelihood == Double.NEGATIVE_INFINITY;
				
			if(logLikelihood > maxVal) {
				maxVal = logLikelihood;
			}
			exponents.set(a_prime, logLikelihood);
		}
		
		// If all moves are fatal, return uniform
		if(maxVal == Double.NEGATIVE_INFINITY) {
			probs.set(1.0);
			return probs.scale( 1.0/probs.elementSum() );
		}
		
		// Calculate probabilities and track sum
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			double normalizedExponent = exponents.get(a_prime) - maxVal;
			double likelihood = Math.exp(normalizedExponent);
			probs.set(a_prime, likelihood);
		}
		
		// Normalize
		probs = probs.divide( probs.elementSum() );
		
		// Additive exploration (epsilon-exploration)
		SimpleMatrix smoother = new SimpleMatrix(moves.length, 1);
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			if(!isFatal[a_prime]) {
				smoother.set(a_prime, 1.0);
			}
		}
		smoother = smoother.scale( _beta/smoother.elementSum() );
		probs = probs.scale(1.0 - _beta);
		probs = probs.plus(smoother);
		probs = probs.divide( probs.elementSum() ); // Should be normalized, but just in case
		
//		System.out.println("PDF:");
//		probs.transpose().print();
//		
//		System.out.println("Exponents:");
//		exponents.transpose().print();
		
		if(probs.hasUncountable()) {
			System.out.println("NaN PDF!");
		}
		
		return probs;
	}

	protected double calculate_log_likelihood(State s, Action a) {
		
		SimpleMatrix temp = _feature.get_feature_vector(s,a);

		// Fatal moves should have 0 probability
		if(temp == null) {
			return Double.NEGATIVE_INFINITY;
		}
		
		return _params.dot(temp)/_temperature;
	}
	
	public double function_evaluator(State s, Action a) {
		return Math.exp(calculate_log_likelihood(s, a));
	}

	public void set_temperature(double temp) {
		_temperature = temp;
	}
	
	public double get_temperature() {
		return _temperature;
	}
	
	public void set_gamma(double gam) {
		_gamma = gam;
	}
	
	public double get_gamma() {
		return _gamma;
	}
	
	public void set_beta(double bet) {
		_beta = bet;
	}
	
	public double get_beta() {
		return _beta;
	}
	
	@Override
	public SimpleMatrix gradient(State s, Action a) {
		SimpleMatrix J_for_a = _feature.get_feature_vector(s, a);
		SimpleMatrix pi_for_s = pi(s);
		
		// If a is a fatal move, we return zero gradient
		if( J_for_a == null) {
			SimpleMatrix ret = new SimpleMatrix( _params.numRows(), 1 );
			ret.set( 0 );
			return ret;
		}
		
		//Each feature vector gets one column
		SimpleMatrix all_features = new SimpleMatrix(J_for_a.numRows(), pi_for_s.numRows());
		all_features.set(0);
		for( int a_prime = 0; a_prime < pi_for_s.numRows(); a_prime++ ) {
			SimpleMatrix feat = _feature.get_feature_vector(s, new Action(a_prime));
			if( feat == null ) {
				continue;
			}
			//Insert the feature vector into the specified column
			all_features.insertIntoThis( 0, a_prime, feat );
		}
		
		SimpleMatrix E_for_s = all_features.mult(pi_for_s);				
		SimpleMatrix grad = J_for_a.minus(E_for_s);
		return grad;
	}
	
	private void normalize_params() {
		double mean = _params.elementSum()/_params.numRows();
		SimpleMatrix shift = new SimpleMatrix( _params.numRows(), 1 );
		shift.set(mean);
		_params = _params.minus( shift );
	}
	
	@Override
	public Policy copy() {
		return new GradientPolicy(this);
	}

}
