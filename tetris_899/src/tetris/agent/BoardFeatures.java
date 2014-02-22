package tetris.agent;

import tetris.simulator.*;
import org.ejml.simple.SimpleMatrix;
import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeatures implements Feature {
	
    public SimpleMatrix get_feature_vector(State s, Action a){
	s.makeMove(a.index);
	board = new SimpleMatrix(s.getField());

	// Set all nonzero values of board to 1
	for(int i=0; i < s.ROWS; i++) {
	    for(int j=0; j < s.COLS; j++) {
		if(board.get(i, j) != 0) {
		    board.set(i, j, 1);
		}
	    }
	}

	// Height of tallest column
	maxH = Arrays.sort(s.getTop())[s.COLS-1];

	// Number of "holes" -- empty slots with a full slot higher in the col
	// Number of empty slots below the top row
	colHeight = s.getTop();
	nHoles = 0;
	nEmptyBelow = 0;
	for(int i=0; i < s.COLS; i++) {
	    h = colHeight[i];
	    nFull = board.extractMatrix(0, h, i-1, i).elementSum();
	    nHoles += h - nFull;
	    nEmptyBelow += maxH - nFull;
	}


	// Average column height?  Minimum column height?
	// Average # of filled spaces in a row?  Number in top row?

	res = [maxH];
	return SimpleMatrix(res);
    }

	// TODO Update as necessary
	public int get_feature_dimension() {
		return 1;
	}

	// TODO Update as necessary
	public Feature copy() {
		Feature ret = new BoardFeatures();
		return ret;
	}

}
