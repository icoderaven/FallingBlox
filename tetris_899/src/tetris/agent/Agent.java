package tetris.agent;
import tetris.simulator.State;
import tetris.trajgen.*;

public class Agent {

	public Policy pi;
	
	public Agent()
	{
		pi = new GradientPolicy();
	}
	//implement this function to have a working system
	//Inputs:
	//- s is the current state of the board
	//- legalMoves is a nx2 matrix of the n possible actions for the current piece.
	//	An action is the orientation & column to place the current piece
	//Outputs:
	//- index n of the action to execute in legalMoves
	public int chooseAction(State s, int[][] legalMoves) 
	{		
		
		// Example code for using trajectory generation package
		StateGenerator stateGen = new FixedStateGenerator(s);
//		Policy policy = new RandomPolicy();
//		RewardFunction rewardFunc = new LinesClearedReward();
		RewardFunction rewardFunc = new TurnsAliveReward();
		TrajectoryGenerator trajGen = 
				new FixedLengthTrajectoryGenerator(stateGen, pi, rewardFunc, 1000);
		
		//trajGen.get_trajectory();

		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		
		pi.fit_policy(trajMachine.generate_trajectories(trajGen, 10));
		
		return pi.get_action(s).index; 
		//return (int)(Math.random()*legalMoves.length);
		
	}
	
}
