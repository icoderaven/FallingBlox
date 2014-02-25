package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.*;

public class Agent {

	public GradientPolicy pi;

	public Agent() {
		 try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV("params.txt");
			Feature feat = new BoardFeature();
			pi = new GradientPolicy(feat, paramMatrix);
			System.out.println("Param log loaded.");
			paramMatrix.transpose().print();
		 } catch(Exception e) {
			 System.out.println("No param log found. Creating new policy.");
			 pi = new GradientPolicy();
		 }
		pi = new GradientPolicy();
		// File to store to
				String logname = "params.txt";
		try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new TopFourFeatures();
			pi = new GradientPolicy(feat, paramMatrix);
			System.out.println("Param log loaded.");
		 } catch(Exception e) {
			 System.out.println("No param log found. Creating new policy.");
		 }
	}

	// implement this function to have a working system
	// Inputs:
	// - s is the current state of the board
	// - legalMoves is a nx2 matrix of the n possible actions for the current
	// piece.
	// An action is the orientation & column to place the current piece
	// Outputs:
	// - index n of the action to execute in legalMoves
	public int chooseAction(State s, int[][] legalMoves) {
		int counter = 1;
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		// Example code for using trajectory generation package
		StateGenerator stateGen = new FixedStateGenerator(s);
		// Policy policy = new RandomPolicy();
		RewardFunction func1 = new LinesClearedReward(1.0);
		RewardFunction func2 = new TurnsAliveReward(0.0);
		RewardFunction rewardFunc = new CompositeReward(func1, func2);
		TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(
				stateGen, pi, rewardFunc, 1000);

		while (counter < 1) {
			pi.fit_policy(trajMachine.generate_trajectories(trajGen, 30));
			counter += 1;
			// return (int)(Math.random()*legalMoves.length);
		}
		Action a = pi.get_action(s);
		int move = a.index;
		trajMachine.close();
		
		SimpleMatrix pdf = pi.pi(s);
		System.out.println("PDF:");
		pdf.transpose().print();
		
		System.out.format("Returning %d%n", move);
		
		double reward = rewardFunc.get_reward(s, a);
		System.out.format("Reward: %f%n", reward);
		
		State copyState = new State(s);
		TopFourFeatures feature = new TopFourFeatures();
		SimpleMatrix features = feature.get_feature_vector(copyState, a);
//		System.out.format("Holes: %f%n", features.get(features.numRows() - 5));
		
		SimpleMatrix params = pi.get_params();
//		System.out.format("Hole weight %f%n", params.get(features.numRows() - 5));
//		System.out.println("Parameters:");
//		params.transpose().print();

		return move;
	}

}
