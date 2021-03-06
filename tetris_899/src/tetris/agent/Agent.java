package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.*;

public class Agent {

	public GradientPolicy pi;
//	public CEPolicy pi;

	public Agent() {
		
//		pi = new CEPolicy();

		String logname = "params.txt";
		try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new AbbeelFeature();
			pi = new GradientPolicy(feat, paramMatrix);
			double startTemp = 1.0;
			double startGamma = 0.99;
			pi.set_temperature(startTemp);
			pi.set_gamma(startGamma);
			pi.set_beta(0.0);
			System.out.println("Param log loaded.");
		 } catch(Exception e) {
			 pi = new GradientPolicy();
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
		
		int a = pi.get_action(s);
		int move = a;

//		SimpleMatrix pdf = pi.pi(s);
//		System.out.println("PDF:");
//		pdf.transpose().print();
//		
//		System.out.format("Returning %d with %f probability%n", move, pdf.get(move));

		return move;
	}

}
