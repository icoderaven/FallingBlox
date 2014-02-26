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
		
		int trajectoryBatchSize = 40;
		int updateBatchSize = 1;
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 0;
		
		
		// File to store to
		String logname = "params.txt";
		
		GradientPolicy pi;
		 try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new BoardFeature();
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

		StateGenerator stateGen = new FixedStateGenerator(startState);
		RewardFunction func1 = new LinesClearedReward(10.0);
		RewardFunction func2 = new TurnsAliveReward(1.0);
		RewardFunction func3 = new DeathReward(-100); // Penalty of -100 for dieing
		RewardFunction comp1 = new CompositeReward(func1, func2);
		RewardFunction rewardFunc = new CompositeReward(comp1, func3);
		
		try {
			while (true) {
				
				TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(stateGen, pi, rewardFunc, maxTrajectoryLength);
				Trajectory[] trajectories = trajMachine.generate_trajectories(trajGen, trajectoryBatchSize);
				
				double step_size = 0.1; // For now...
				
				for(int i = 0; i < updateBatchSize; i++) {	
					pi.fit_policy(trajectories, step_size);
				}
				
				SimpleMatrix parameters = pi.get_params();
				
				parameters.transpose().print();
				parameters.saveToFileCSV(logname);
			
				System.out.format("Ran %d iterations so far.%n", updateIterationCounter);
				updateIterationCounter++;
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		trajMachine.close();
		
	}
	
}
