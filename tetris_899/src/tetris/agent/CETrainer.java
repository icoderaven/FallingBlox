package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;
import tetris.trajgen.CEFixedLengthTrajectoryGenerator;
import tetris.trajgen.CETrajectoryGenerationPool;
import tetris.trajgen.StateGenerator;
import tetris.trajgen.PolicyStateGenerator;
import tetris.trajgen.CETrajectoryGenerator;

public class CETrainer {
	
	public static void main(String[] args) {
		
		int trajectoryBatchSize = 100;
		int maxTrajectoryLength = 10000;
		int updateIterationCounter = 0;
		
		String logname = "ce_params.txt";
		String mulogname = "ce_mu.txt";
		String sigmalogname = "ce_sigma.txt";
		
		CEPolicy pi = new CEPolicy();
		
		State startState = new State();
		
		CETrajectoryGenerationPool trajMachine = new CETrajectoryGenerationPool(8); // # threads		
		
		Policy trainerPi = new RandomPolicy();
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi, startState, 0);
		RewardFunction func1 = new LinesClearedReward(1.0);
		RewardFunction func2 = new TurnsAliveReward(0.0);
		RewardFunction func3 = new DeathReward(-10.0); // Penalty for dying
		RewardFunction comp1 = new CompositeReward(func1, func2);
		RewardFunction rewardFunc = new CompositeReward(comp1, func3);

		try {
			while (true) {
				CETrajectoryGenerator trajGen = new CEFixedLengthTrajectoryGenerator(stateGen, pi, 
						rewardFunc, maxTrajectoryLength);
				CETrajectory[] trajectories = trajMachine.generate_trajectories(trajGen, 
						trajectoryBatchSize);
				
//				pi.fit_policy(trajectories, stepSize);
				pi.fit_policy(trajectories);
				updateIterationCounter++;
				
				SimpleMatrix parameters = pi.get_params();
//				parameters.print();
				parameters.saveToFileCSV(logname);
				
				pi.getMu().saveToFileCSV(mulogname);
				pi.getSigma().saveToFileCSV(sigmalogname);
				
				System.out.format("Ran %d iterations so far.", updateIterationCounter);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		trajMachine.close();
		
	}

}
