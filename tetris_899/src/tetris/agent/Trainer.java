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
		
		int trajectoryBatchSize = 16;
		int updateBatchSize = 1;
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 0;
		
		//double[][] initParams = {{-67.614,  -149.568,  -132.615,  -104.042,  -99.701,  -101.322,  -102.901,  -117.513,  -137.698,  -123.980,  -169.621,  -148.730,  -144.496,  -153.810,  -158.789,  -163.900,  -160.824,  -154.611,  -130.049,  -134.479,  59.012,  60.905,  60.560,  49.109,  43.300,  31.423,  47.733,  39.732,  65.613,  46.745,  53.173,  41.362,  35.020,  37.169,  29.694,  35.889,  33.417,  25.372, 6.008, 1.709,   0.000,  -323.980,  -67.614,  -2151.931,  -162.215,  -313.695,   0.000,  -10.039}}; 
//		SimpleMatrix paramMatrix = new SimpleMatrix(initParams);
//		Feature feat = new BoardFeature();
//		GradientPolicy pi = new GradientPolicy(feat, paramMatrix.transpose());
		GradientPolicy pi = new GradientPolicy();
		
		SimpleMatrix pars = pi.get_params();
		pars.transpose().print();
		
		RandomPolicy trainerPi = new RandomPolicy();

		State startState = new State();
		
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		
		//StateGenerator stateGen = new FixedStateGenerator(startState);
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi, startState, trainerSteps);
		
		RewardFunction func1 = new LinesClearedReward(1.0);
		RewardFunction func2 = new TurnsAliveReward(0.1);
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
		} catch(Exception e) {
			e.printStackTrace();
		}
			
		trajMachine.close();
		
		if (log != null) {
			log.close();
		}
		
	}
	
}
