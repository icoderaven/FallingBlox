package tetris.trajgen;

import org.ejml.simple.SimpleMatrix;

public class GradientResult {

	public SimpleMatrix gradient;
	public SimpleMatrix covariance;
	public int numSteps;
	public double reward;
	public int numRows;
	
	public GradientResult(SimpleMatrix grad, SimpleMatrix cov, int steps, double rew, int rows) {
		gradient = grad;
		covariance = cov;
		numSteps = steps;
		reward = reward;
		numRows = rows;
	}
	
}
