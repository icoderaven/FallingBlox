package tetris.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.FixedLengthTrajectoryGenerator;
import tetris.trajgen.FixedStateGenerator;
import tetris.trajgen.GradientResult;
import tetris.trajgen.PolicyStateGenerator;
import tetris.trajgen.StateGenerator;
import tetris.trajgen.TrajectoryGenerationPool;
import tetris.trajgen.TrajectoryGenerator;
import tetris.trajgen.GradientCalculationPool;

public class Trainer {

	public static void main(String[] args) {
		
		int trajectoryBatchSize = 64; // 2 x num parameters
		int updateBatchSize = 1; // # steps to run before decreasing step size, temp, gamma, etc.
		int updateIterationCounter = 0;
		int maxTrajectoryLength = (int) 1E9;
		int trainerSteps = 0;
		
		
		// File to store to
		String logname = "params.txt";
		String metalogname = "metaparams.txt";
		
		// Load the policy
		GradientPolicy pi;
		try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new AbbeelFeature();
			pi = new GradientPolicy(feat, paramMatrix);
			System.out.println("Param log loaded.");
		 } catch(Exception e) {
			 System.out.println("No param log found. Creating new policy.");
			 pi = new GradientPolicy();
		 }
		 
		// Load the metaparameters
		SimpleMatrix metaparams;
		 try {
				metaparams = SimpleMatrix.loadCSV(metalogname);
				System.out.format("Metaparameter log loaded. Starting from iteration %f%n", metaparams.get(0));
			 } catch(Exception e) {
				 System.out.println("No metaparam log found. Starting new trainer.");
				 metaparams = new SimpleMatrix(1,1);
			 }
		
		SimpleMatrix pars = pi.get_params();
		pars.transpose().print();

		State startState = new State();
		
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8); // # threads
		GradientCalculationPool gradMachine = new GradientCalculationPool(8);

//		StateGenerator stateGen = new FixedStateGenerator(startState);
		Policy trainerPi = new RandomPolicy();
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi,
				startState, trainerSteps);
		RewardFunction func1 = new LinesClearedReward(10.0);
		RewardFunction func2 = new TurnsAliveReward(1);
		RewardFunction func3 = new DeathReward(-100); // Penalty of -100 for
														// dieing
		RewardFunction comp1 = new CompositeReward(func1, func2);
		RewardFunction rewardFunc = new CompositeReward(comp1, func3);
		
		double startTemp = 1.0;
		pi.set_temperature(startTemp);
		
		double startStepSize = 1.0;
		double stepSize = startStepSize;
		
		double startGamma = 0.99;
		double gammaConstant = -0.01; // 30 iterations to decay to gamma = 0.95
		pi.set_gamma(startGamma);
		
		double startBeta = 0.01; // % of random non-fatal moves
		double betaConstant = -0.01;
//		pi.set_beta(startBeta);
		
		try {
			while (true) {
				for(int i = 0; i < updateBatchSize; i++) {
					TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(stateGen, pi, rewardFunc, maxTrajectoryLength);
					Trajectory[] trajectories = trajMachine.generate_trajectories(trajGen, trajectoryBatchSize);
					GradientResult[] gradients = gradMachine.calculate_gradients(pi, trajectories);
					
//					pi.fit_policy(trajectories, 0.1);
					pi.fit_policy(gradients, 0.1);
				}
				updateIterationCounter++;
				
				SimpleMatrix parameters = pi.get_params();
				
				parameters.transpose().print();
				parameters.saveToFileCSV(logname);
			
				metaparams.set(0, (double) updateIterationCounter);
				metaparams.saveToFileCSV(metalogname);
				
				// Reduce step size gradually according to 1/i
				stepSize = startStepSize/updateIterationCounter;
				
				// Reduce randomness with various parameters at each step
				double nextTemperature = startTemp/updateIterationCounter;
//				pi.set_temperature(nextTemperature);
				
				double nextGamma = (1.0 - startGamma*Math.exp(updateIterationCounter*gammaConstant));
//				pi.set_gamma( nextGamma );
				
				double nextBeta = startBeta*Math.exp(updateIterationCounter*betaConstant);
//				pi.set_beta( nextBeta );
				
				System.out.format("Ran %d iterations so far. Step Size: %f, Temp: %f, Gamma: %f, Beta:%f%n", 
				updateIterationCounter, stepSize, nextTemperature, nextGamma, nextBeta);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		trajMachine.close();
		
	}
	
}
