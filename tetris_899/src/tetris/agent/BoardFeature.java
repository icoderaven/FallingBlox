package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeature implements Feature {
		
	public int nFeatures = 6;
	
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
		
		// Height of tallest column, shortest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		int maxH = temp[temp.length-1];
		int minH = temp[0];

		// Average column height
		// Number of "holes" -- empty slots with a full slot higher in the col
		// Number of empty slots below the top row
		int[] colHeight = s.getTop();
		int nHoles = 0;
		int nEmptyBelow = 0;
		double totalHeight = 0.0;
		
		for(int i=0; i < s.COLS; i++) {
			int h = colHeight[i];
			SimpleMatrix col = board.extractVector(false, i);
			int nFullCol = (int) (col.elementSum());
			nHoles += h - nFullCol;
			nEmptyBelow += maxH - nFullCol;
			totalHeight += h;
		}
		
		double avgH = totalHeight / s.COLS;

		// Size of most-filled row, least, average (want to take average below maxH?)
		int[] rowFill = new int[s.ROWS];
		double totalRowFill = 0.0;
		for(int i=0; i < s.ROWS; i++) {
			SimpleMatrix row = board.extractVector(true, i);
			int nFullRow = (int) (row.elementSum());
			rowFill[i] = nFullRow;
			totalRowFill += nFullRow;
		}
		Arrays.sort(rowFill);
		int minRow = rowFill[0];
		int maxRow = rowFill[rowFill.length - 1];
		double avgRow = totalRowFill / s.ROWS;
		
		// Rows removed
		int rGone = s.getRowsCleared();
		
		// Number of filled spaces in top row?
		// Number of overhangs? (filled spot over empty spot)
		
		// Return the features.  Can change which features are used by 
		// changing this array assignment and nFeatures above
		double rows = (double) s.ROWS;
		double cols = (double) s.COLS;
		double[][] resTemp = {{maxH/rows, minH/rows, nHoles/(rows*cols), nEmptyBelow/(rows*cols),  minRow/(rows), maxRow/(rows)}};
		SimpleMatrix res = new SimpleMatrix(resTemp);
		res = res.transpose();
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
