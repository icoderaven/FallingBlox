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
		
		int trajectoryBatchSize = 32;
		int updateBatchSize = 1;
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 4;
		
		
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
		
		RandomPolicy trainerPi = new RandomPolicy();

		State startState = new State();
		
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		
		//StateGenerator stateGen = new FixedStateGenerator(startState);
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi, startState, trainerSteps);
		
		RewardFunction func1 = new LinesClearedReward(1.0);
		RewardFunction func2 = new TurnsAliveReward(0.0);
		RewardFunction rewardFunc = new CompositeReward(func1, func2);
		
		TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(
				stateGen, pi, rewardFunc, maxTrajectoryLength);

		try {
			while (true) {
				for(int i = 0; i < updateBatchSize; i++) {
					pi.fit_policy(trajMachine.generate_trajectories(trajGen, trajectoryBatchSize));
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
