package tetris.agent;

import java.util.*;

import org.ejml.simple.SimpleMatrix;
import org.ejml.ops.*;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import tetris.simulator.*;

public class CEPolicy implements Policy {
	
	public Feature _feature;
	private SimpleMatrix _params;
	private SimpleMatrix _gaussMu;
	private SimpleMatrix _gaussSigma;
	private double _gamma;
	private double _beta;
	private boolean _useMultivar;
	
	public CEPolicy() {
		_feature = new AbbeelFeature();
		//_feature = new CEFeature();
		//_feature = new BoardFeature();
		//_feature = new DefaultFeature();
		_params = new SimpleMatrix(_feature.get_feature_dimension(),1);
		_params.set(0.0);
		
		_gaussMu = new SimpleMatrix(_feature.get_feature_dimension(), 1);
		_gaussMu.zero();
		
		_useMultivar = false;
		if(_useMultivar) {
			_gaussSigma = SimpleMatrix.identity(_feature.get_feature_dimension());
		} else {
			_gaussSigma = new SimpleMatrix(_gaussMu);
		}
		
		_gamma = 0.95;
		_beta = 0.01;
	}
	
	public CEPolicy(CEPolicy other) {
		_feature = other._feature.copy();
		_params = new SimpleMatrix(other._params);
		_gaussMu = new SimpleMatrix(other._gaussMu);
		_gaussSigma = new SimpleMatrix(other._gaussSigma);
		_gamma = other._gamma;
		_beta = other._beta;
	}

	@Override
	public CEPolicy copy() {
		return new CEPolicy(this);
	}

	@Override
	public void fit_policy(Trajectory[] t) {
	}
	
	public void fit_policy(CETrajectory[] t) {
		int nTraj = t.length;
        Arrays.sort(t, new Comparator<CETrajectory>() {
        	public int compare(CETrajectory t1, CETrajectory t2) {
        		double a = t1.sum_rewards_tail(0, 1.0);
        		double b = t2.sum_rewards_tail(0, 1.0);
                return ((Double) a).compareTo(b);
        	}
        });
		
		// Pick the top 10 and make them the Elite set
		int nElite = Math.min(10, nTraj);
		CETrajectory[] elite = Arrays.copyOfRange(t, nTraj - nElite, nTraj);
		
		
		// Compute new mu and sigma for each feature, save them
		SimpleMatrix oldMu = new SimpleMatrix(_gaussMu);
		_gaussMu.zero();
		for(int i = 0; i < nElite; i++) {
			_gaussMu = _gaussMu.plus(elite[i].params);
//			elite[i].params.print();
		}
		_gaussMu = _gaussMu.scale(1.0/nElite);
		
//		_gaussMu.print();
		
		_gaussSigma.zero();
		for(int i = 0; i < nElite; i++) {
			//SimpleMatrix temp = (elite[i].params).minus(_gaussMu);
			SimpleMatrix temp = (elite[i].params).minus(oldMu);
//			temp.print();
			if(_useMultivar) {
				_gaussSigma = _gaussSigma.plus(temp.mult(temp.transpose()));
			} else {
				CommonOps.elementMult(temp.getMatrix(), temp.getMatrix(), _gaussSigma.getMatrix()); 
			}
		}
		_gaussSigma.scale(1.0/nElite);
		
//		_gaussSigma.print();
		
		_params = sampleParams();
	}
	
	public SimpleMatrix sampleParams() {
		// Take a sample from the current Gaussian
		int n = _feature.get_feature_dimension();
		
		if(_useMultivar) {
			double[] tempMu = new double[n];
			double[][] tempSigma = new double[n][n];
			
			for(int i = 0; i < n; i++) {
				tempMu[i] = _gaussMu.get(i);
				
				for(int j = 0; j < n; j++) {
					tempSigma[i][j] = _gaussSigma.get(i, j);
				}
			}
					
			MultivariateNormalDistribution dist = new MultivariateNormalDistribution(tempMu, tempSigma);
			double[][] temp = new double[1][n]; 
			temp[0] = dist.sample();
			SimpleMatrix sample = new SimpleMatrix(temp);
			
			return sample.transpose();
		} else {
			SimpleMatrix sample = new SimpleMatrix(n, 1);
			Random rng = new Random();	
			for(int i = 0; i < n; i++) {
				double temp = rng.nextGaussian() * _gaussSigma.get(i) + _gaussMu.get(i);
				sample.set(i, temp);
			}
			
			return sample;
		}
	}
	
	public SimpleMatrix getMu() {
		return new SimpleMatrix(_gaussMu);
	}
	
	public SimpleMatrix getSigma() {
		return new SimpleMatrix(_gaussSigma);
	}
	

	@Override
	public SimpleMatrix gradient(State s, int a) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	// Copied from GradientPolicy
	
	public SimpleMatrix get_params() {
		return new SimpleMatrix(_params);
	}
	
	@Override
	public int get_action(State curr_state) {
		
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
		
		return index;
		
	}
	
	@Override
	public double pi(State s, int a) {
		SimpleMatrix dist = pi(s);
		return dist.get(a);
	}
	
	@Override
	public SimpleMatrix pi(State s) {
		
		/// For current piece get all possible actions
		int[][] moves = s.legalMoves();
		boolean[] isFatal = new boolean[moves.length]; 
		SimpleMatrix exponents = new SimpleMatrix(moves.length, 1);
		SimpleMatrix probs = new SimpleMatrix(moves.length, 1);

		// Get all exponents first
		double sum = 0.0;
		double maxVal = Double.NEGATIVE_INFINITY;
		int numValid = 0;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			
			double logLikelihood = calculate_log_likelihood(s, a_prime);
			isFatal[a_prime] = logLikelihood == Double.NEGATIVE_INFINITY;
				
			if(logLikelihood > maxVal) {
				maxVal = logLikelihood;
			}
			
			if(logLikelihood != Double.NEGATIVE_INFINITY) {
				sum += logLikelihood;
				numValid++;
			}
			exponents.set(a_prime, logLikelihood);
		}
		
//		exponents.transpose().print();
		
		// If all moves are fatal, return uniform
		if(maxVal == Double.NEGATIVE_INFINITY) {
			probs.set(1.0);
			return probs.scale( 1.0/probs.elementSum() );
		}
		
		// Calculate probabilities and track sum
		double meanVal = sum/numValid;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			double normalizedExponent = exponents.get(a_prime) - maxVal + 10.0;
//			double normalizedExponent = exponents.get(a_prime) - meanVal;
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

	protected double calculate_log_likelihood(State s, int a) {
		SimpleMatrix temp = _feature.get_feature_vector(s,a);

		// Fatal moves should have 0 probability
		if(temp == null) {
			return Double.NEGATIVE_INFINITY;
		}
		
		return _params.dot(temp); // should normalize?
	}

	
	// To get action based on a particular set of weights
	public int get_action(State curr_state, SimpleMatrix params) {
		
		//Obtain the distribution of probabilities for the current action 
		SimpleMatrix dist = pi(curr_state, params);
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
		
		return index;
		
	}	
	
	public SimpleMatrix pi(State s, SimpleMatrix params) {
		
		/// For current piece get all possible actions
		int[][] moves = s.legalMoves();
		boolean[] isFatal = new boolean[moves.length]; 
		SimpleMatrix exponents = new SimpleMatrix(moves.length, 1);
		SimpleMatrix probs = new SimpleMatrix(moves.length, 1);

		// Get all exponents first
		double sum = 0.0;
		double maxVal = Double.NEGATIVE_INFINITY;
		int numValid = 0;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			
			double logLikelihood = calculate_log_likelihood(s, a_prime, params);
			isFatal[a_prime] = logLikelihood == Double.NEGATIVE_INFINITY;
				
			if(logLikelihood > maxVal) {
				maxVal = logLikelihood;
			}
			
			if(logLikelihood != Double.NEGATIVE_INFINITY) {
				sum += logLikelihood;
				numValid++;
			}
			exponents.set(a_prime, logLikelihood);
		}
		
//		exponents.transpose().print();
		
		// If all moves are fatal, return uniform
		if(maxVal == Double.NEGATIVE_INFINITY) {
			probs.set(1.0);
			return probs.scale( 1.0/probs.elementSum() );
		}
		
		// Calculate probabilities and track sum
		double meanVal = sum/numValid;
		for (int a_prime = 0; a_prime < moves.length; a_prime++) {
			double normalizedExponent = exponents.get(a_prime) - maxVal + 10.0;
//			double normalizedExponent = exponents.get(a_prime) - meanVal;
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

	protected double calculate_log_likelihood(State s, int a, SimpleMatrix params) {
		SimpleMatrix temp = _feature.get_feature_vector(s,a);

		// Fatal moves should have 0 probability
		if(temp == null) {
			return Double.NEGATIVE_INFINITY;
		}
		
		return params.dot(temp); // should normalize?
	}
	
}
