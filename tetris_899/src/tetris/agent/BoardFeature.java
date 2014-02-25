package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeature implements Feature {
		
	public int nFeatures = 9; // + 2*State.COLS + State.ROWS;
	
    public SimpleMatrix get_feature_vector(State temp_s, Action a)
    {    
    	// Prepare to return the features
		double rows = (double) State.ROWS;
		double cols = (double) State.COLS;
		double[][] resTemp = new double[1][nFeatures];
		// Feature vector is height of each col, # of holes in each col, number of 
		// filled spaces in each row, height of tallest col, height of shortest col,
		// total number of holes, number of empty spaces below the highest point 
		// on the board, avg height of columns, number of filled spaces in emptiest 
		// row, number filled in fullest row, avg of rows
    	
    	// Look at the board after taking the action
		State s = new State(temp_s);
		int numRowsCleared = s.getRowsCleared();
		
		s.makeMove(a.index);
    	int erodedRows = s.getRowsCleared() - numRowsCleared;
		
    	// Extract the board and set all nonzero values to 1
		SimpleMatrix board = new SimpleMatrix(State.ROWS, State.COLS) ;
		int[][] field = s.getField();
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				board.set(i, j,  (double)Math.min(field[i][j], 1));
			}
		}

		// Compute the features:
		
		// Height of tallest column, shortest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		double maxH = temp[temp.length-1];
		double minH = temp[0];

		// Average column height
		// Number of "holes" -- empty slots with a full slot higher in the col
		// Number of empty slots below the top row
		// Height and number of holes for each column
		int[] colHeight = s.getTop();
		double[] colHoles = new double[s.COLS];
		double nHoles = 0;
		double nEmptyBelow = 0;
		double totalHeight = 0.0;
		
		for(int i=0; i < s.COLS; i++) {
			double h = colHeight[i];
			SimpleMatrix col = board.extractVector(false, i);
			double nFullCol = (int) (col.elementSum());
			nHoles += h - nFullCol;
			colHoles[i] = h - nFullCol;
			nEmptyBelow += maxH - nFullCol;
			totalHeight += h;
			
			//resTemp[0][i] = h/rows;
			//resTemp[0][State.COLS + i] = (h - nFullCol)/rows;
		}
		
		double avgH = totalHeight / s.COLS;

		// Size of most-filled row, least, average (want to take average below maxH?)
		double[] rowFill = new double[s.ROWS];
		double totalRowFill = 0.0;
		for(int i=0; i < s.ROWS; i++) {
			SimpleMatrix row = board.extractVector(true, i);
			double nFullRow = row.elementSum();
			rowFill[i] = nFullRow;
			totalRowFill += nFullRow;
			
			//resTemp[0][2*State.COLS + i] = nFullRow/cols;
		}
		Arrays.sort(rowFill);
		double minRow = rowFill[0];
		double maxRow = rowFill[rowFill.length - 1];
		double avgRow = totalRowFill / s.ROWS;
		
		// Rows removed
		//int rGone = s.getRowsCleared();
		
		// Number of filled spaces in top row?
		// Number of overhangs? (filled spot over empty spot)
		
		// Return the features.  Can change which features are used by 
		// changing assignments to resTemp and nFeatures above
		//int curInd = 2*State.COLS + State.ROWS;
		int curInd = 0;
		double[] indivFeatures =  {maxH, minH, nHoles, 
			nEmptyBelow,  avgH, minRow, maxRow, avgRow, erodedRows};
		System.arraycopy(indivFeatures, 0, resTemp[0], curInd, indivFeatures.length);
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
