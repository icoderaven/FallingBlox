package tetris.agent;

import tetris.simulator.State;

public class RandomPolicy implements Policy {
	@Override
	public int get_action(State curr_state) {
		// return random action
		int[][] moves =  curr_state.legalMoves();
		//Get a random move from list of moves nx2
		int move_index = (int) (Math.random()*moves.length);
//		return new Action(moves[move_index][0], moves[move_index][1]) ;
		return move_index;
	}
	
	@Override
	public void fit_policy(State[] s_t, Action[] a_t, State[] s_tplus) {
		//Do nothing - We're random
	}

}