package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

public class RandomPolicy implements Policy {
	
	@Override
	public Action get_action(State curr_state) {
		// return random action
		int[][] moves =  curr_state.legalMoves();
		//Get a random move from list of moves nx2
		int move_index = (int) (Math.random()*moves.length);
//		return new Action(moves[move_index][0], moves[move_index][1]) ;
		return new Action(move_index);
	}
	

	@Override
	public double pi(State s, Action a) {
		return 1.0;
	}

	@Override
	public SimpleMatrix gradient(State s, Action a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleMatrix pi(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	// Return a deep copy
	public Policy copy() {
		Policy ret = new RandomPolicy();
		return ret;
	}


	@Override
	public void fit_policy(Trajectory[] t) {
		// Do Nothing. We're random
		
	}

}