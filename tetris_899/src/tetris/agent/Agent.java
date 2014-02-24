package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.*;

public class Agent {

	public GradientPolicy pi;

	public Agent() {
		pi = new GradientPolicy();
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
		int counter = 0;
		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8);
		while (counter < 10) {

			// Example code for using trajectory generation package
			StateGenerator stateGen = new FixedStateGenerator(s);
			// Policy policy = new RandomPolicy();
			RewardFunction func1 = new LinesClearedReward(10.0);
			RewardFunction func2 = new TurnsAliveReward(1.0);
			RewardFunction rewardFunc = new CompositeReward(func1, func2);
			TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(
					stateGen, pi, rewardFunc, 100);

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
		
		State copyState = new State(s);
		BoardFeature feature = new BoardFeature();
		SimpleMatrix features = feature.get_feature_vector(copyState, a);
		System.out.format("Holes: %f%n", features.get(features.numRows() - 5));
		
		SimpleMatrix params = pi.get_params();
		System.out.format("Hole weight %f%n", params.get(features.numRows() - 5));
		System.out.println("Parameters:");
		params.transpose().print();
		pi._normalizedParams.transpose().print();

		return move;
	}

}
