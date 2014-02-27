package tetris.agent;

import java.io.IOException;
import java.io.PrintWriter;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.FixedLengthTrajectoryGenerator;
import tetris.trajgen.FixedStateGenerator;
import tetris.trajgen.PolicyStateGenerator;
import tetris.trajgen.StateGenerator;
import tetris.trajgen.TrajectoryGenerationPool;
import tetris.trajgen.TrajectoryGenerator;

public class Trainer {

	public static void main(String[] args) {
		
		int trajectoryBatchSize = 64; // 2 x num parameters
		int updateBatchSize = 1; // # steps to run before decreasing step size, temp, gamma, etc.
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 10;
		
		
		// File to store to
		String logname = "params.txt";
		
		GradientPolicy pi;
		 try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new TopFourFeatures();
			pi = new GradientPolicy(feat, paramMatrix);
			System.out.println("Param log loaded.");
		 } catch(Exception e) {
			 System.out.println("No param log found. Creating new policy.");
			 pi = new GradientPolicy();
		 }
		
		SimpleMatrix pars = pi.get_params();
		pars.transpose().print();

		State startState = new State();
		
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8); // # threads

//		StateGenerator stateGen = new FixedStateGenerator(startState);
		Policy trainerPi = new RandomPolicy();
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi, startState, trainerSteps);
		RewardFunction func1 = new LinesClearedReward(10.0);
		RewardFunction func2 = new TurnsAliveReward(0.1);
		RewardFunction func3 = new DeathReward(-100); // Penalty of -100 for dieing
		RewardFunction comp1 = new CompositeReward(func1, func2);
		RewardFunction rewardFunc = new CompositeReward(comp1, func3);
		
		double startTemp = 1.0;
		pi.set_temperature(startTemp);
		
		double startStepSize = 1.0;
		
		double startGamma = 0.99;
		double gammaConstant = -0.01; // 30 iterations to decay to gamma = 0.95
		pi.set_gamma(startGamma);
		
		try {
			while (true) {
				for(int i = 0; i < updateBatchSize; i++) {
					
					TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(stateGen, pi, rewardFunc, maxTrajectoryLength);
					Trajectory[] trajectories = trajMachine.generate_trajectories(trajGen, trajectoryBatchSize);
					
//					pi.fit_policy(trajectories, startStepSize/(updateIterationCounter+1));
					pi.fit_policy(trajectories, 1.0);
					stateGen = new PolicyStateGenerator(pi, startState, (int)Math.random()*trainerSteps);
				}
				
				SimpleMatrix parameters = pi.get_params();
				
				parameters.transpose().print();
				parameters.saveToFileCSV(logname);
			
				updateIterationCounter++;
				
				// Reduce temperature at each step
//				pi.set_temperature(startTemp/updateIterationCounter);
//				double nextGamma = (1.0 - startGamma*Math.exp(updateIterationCounter*gammaConstant));
//				pi.set_gamma( nextGamma );
				
//				System.out.format("Ran %d iterations so far. Gamma: %f%n", updateIterationCounter, nextGamma);
				System.out.format("Ran %d iterations so far.%n", updateIterationCounter);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		trajMachine.close();
		
	}
	
}
