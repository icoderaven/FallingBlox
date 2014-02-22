package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeature implements Feature {
		
	public int nFeatures = 3;
	
    public SimpleMatrix get_feature_vector(State temp_s, Action a)
    {    	
    	// Look at the board after taking the action
		State s = new State(temp_s);
		s.makeMove(a.index);
    	
    	// Extract the board and set all nonzero values to 1
		SimpleMatrix board = new SimpleMatrix(State.ROWS, State.COLS) ;
		int[][] field = s.getField();
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				board.set(i, j,  Math.min(field[i][j], 1));
			}
		}

		// Compute the features:
		
		// Height of tallest column
		int[] temp = s.getTop();
		System.out.println(Arrays.toString(temp));
		Arrays.sort(temp);
		int maxH = temp[temp.length-1];

		// Number of "holes" -- empty slots with a full slot higher in the col
		// Number of empty slots below the top row
		int[] colHeight = s.getTop();
		int nHoles = 0;
		int nEmptyBelow = 0;
		for(int i=0; i < s.COLS; i++) {
			int h = colHeight[i];
			SimpleMatrix col = board.extractVector(false, i);
			int nFullCol = (int) (col.elementSum());
			nHoles += h - nFullCol;
			nEmptyBelow += maxH - nFullCol;
		}


		// Average column height?  Minimum column height?
		// Average # of filled spaces in a row?  Number in top row?
		double[][] resTemp = {{maxH}, {nHoles}, {nEmptyBelow}};
		SimpleMatrix res = new SimpleMatrix(resTemp);
		//res.print();
		return res;
    }

	// TODO Update as necessary
	public int get_feature_dimension() {
		return nFeatures;
	}

	// TODO Update as necessary
	public Feature copy() {
		Feature ret = new BoardFeature();
		return ret;
	}

}
