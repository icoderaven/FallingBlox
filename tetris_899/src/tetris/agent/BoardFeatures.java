package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeatures implements Feature {
	
	public int nFeatures;
	
    public SimpleMatrix get_feature_vector(State s, Action a)
    {
    	// Look at the board after taking the action
    	s.makeMove(a.index);
    	
    	// Extract the board and set all nonzero values to 1
		SimpleMatrix board = new SimpleMatrix(State.COLS*State.ROWS, 1) ;
		int[][] field = s.getField();
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				board.set(i*board.numCols()+j ,  Math.min(field[i][j], 1));
			}
		}

		// Compute the features:
		
		// Height of tallest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		int maxH = temp[temp.length-1];

		// Number of "holes" -- empty slots with a full slot higher in the col
		// Number of empty slots below the top row
		int[] colHeight = s.getTop();
		int nHoles = 0;
		int nEmptyBelow = 0;
		for(int i=0; i < s.COLS; i++) {
			int h = colHeight[i];
			int nFullCol = (int) board.extractMatrix(0, h, i-1, i).elementSum();
			nHoles += h - nFullCol;
			nEmptyBelow += maxH - nFullCol;
		}


		// Average column height?  Minimum column height?
		// Average # of filled spaces in a row?  Number in top row?
		nFeatures = 3;
		SimpleMatrix res = new SimpleMatrix(nFeatures, 1);
		res.setRow(0, maxH, nHoles, nEmptyBelow);
		return res;
    }

	// TODO Update as necessary
	public int get_feature_dimension() {
		return nFeatures;
	}

	// TODO Update as necessary
	public Feature copy() {
		Feature ret = new BoardFeatures();
		return ret;
	}

}
