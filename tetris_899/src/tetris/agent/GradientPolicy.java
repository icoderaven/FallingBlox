package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

public class GradientPolicy implements Policy {
	
	public Feature _feature;
	private SimpleMatrix _params;
	@Override
	public int get_action(State curr_state) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void fit_policy(State[] s_t, Action[] a_t, State[] s_tplus) {
		// TODO Auto-generated method stub

	}

	@Override
	public double pi(SimState s, Action a) {
		double numerator = function_evaluator(s, a);
		
		double denominator = 0;
		//For all pieces
		for(int piece=0; piece < s.get_n_pieces(); piece++)
		{
			s.set_next_piece(piece);

			//Get all possible actions
			int[][] moves =  s.legalMoves();
			
			//Evaluate function for all actions
			for(int a_prime=0; a_prime < moves.length; a++)
			{
				denominator += function_evaluator(s, Action(moves[a_prime]));
			}
			
		}
		
		return 0;
	}
	
	public double function_evaluator(State s, Action a){
		return Math.exp( _params.dot(_feature.get_feature_vector(s, a)) );
	}

	@Override
	public double[] gradient(State s, Action a) {
		// TODO Auto-generated method stub
		return null;
	}

}
