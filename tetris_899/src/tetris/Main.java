package tetris;

import tetris.simulator.*;
import tetris.agent.*;

public class Main {

	public static void main(String[] args) {
		State sFinal = runGraphics();
		System.out.println("You have completed "+sFinal.getRowsCleared()+" rows.");
	}
	
	//run the tetris game and save image of the board at each turn, returns the final state
	public static State recordVideo()
	{
		int delay = 500;
		
		State s = new State();
		Visualizer v = new Visualizer(s);
		Agent a = new Agent();
		while(!s.hasLost()) {
			s.makeMove(a.chooseAction(s,s.legalMoves()));
			v.draw();
			v.drawNext(0,0);
			v.save(s.getTurnNumber() + ".png");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		v.dispose();
		return s;
	}
	
	
	//runs the tetris game until the game is over and returns the final state
	public static State run()
	{
		State s = new State();
		Agent a = new Agent();
		while(!s.hasLost()) {
			s.makeMove(a.chooseAction(s,s.legalMoves()));
		}
		return s;
	}
	
	//runs and displays the tetris game until the game is over and returns the final state
	public static State runGraphics()
	{
		int delay = 1;
		
		State s = new State();
		Visualizer v = new Visualizer(s);
		Agent a = new Agent();
		while(!s.hasLost()) {
			s.makeMove(a.chooseAction(s,s.legalMoves()));
			v.draw();
			v.drawNext(0,0);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		v.dispose();
		return s;
	}
	
	//allows a human player to play using the 4 arrow keys
	// left: move the piece left
	// right: move the piece right
	// top: change orientation
	// down: drop piece
	// there is no time limit for choosing where to place the next piece
	public static State runHumanPlayer()
	{
		int delay = 100;
		State s = new State();
		Visualizer v = new Visualizer(s);
		v.draw();
		v.drawNext(0,0);
		while(!s.hasLost()) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		v.dispose();
		return s;
	}
}
