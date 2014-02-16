package tetris.agent;
import tetris.simulator.State;


public class Agent {

	public Policy pi;
	
	public Agent()
	{
		pi = new RandomPolicy();
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
		//example random agent
		return pi.get_action(s); 
		//return random action
//		return (int)(Math.random()*legalMoves.length);
	}
	
}
