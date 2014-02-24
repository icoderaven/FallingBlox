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
		
		int trajectoryBatchSize = 30;
		int updateBatchSize = 10;
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 4;
		
		GradientPolicy pi = new GradientPolicy();
		RandomPolicy trainerPi = new RandomPolicy();

		State startState = new State();
		
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		
		//StateGenerator stateGen = new FixedStateGenerator(startState);
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi, startState, trainerSteps);
		
		RewardFunction func1 = new LinesClearedReward(10.0);
		RewardFunction func2 = new TurnsAliveReward(1.0);
		RewardFunction rewardFunc = new CompositeReward(func1, func2);
		
		TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(
				stateGen, pi, rewardFunc, maxTrajectoryLength);
		
		// File to store to
		String logname = "params.txt";
		PrintWriter log = null;
		
		try {
			while (true) {
				for(int i = 0; i < updateBatchSize; i++) {
					pi.fit_policy(trajMachine.generate_trajectories(trajGen, trajectoryBatchSize));
				}
				SimpleMatrix parameters = pi.get_params();
				
				String paramString = parameters.transpose().toString();
				
				log = new PrintWriter(logname);
				log.println(paramString);
				log.close();
				System.out.println(paramString);
			
				System.out.format("Ran %d iterations so far.%n", updateIterationCounter);
				updateIterationCounter++;
			}
		} catch(Exception e) {}
			
		trajMachine.close();
		
		log.close();
		
	}
	
}
