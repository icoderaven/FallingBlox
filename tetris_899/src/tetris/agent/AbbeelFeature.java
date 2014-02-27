package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class AbbeelFeature implements Feature {
		
	public int nFeatures = 22; // + 2*State.COLS + State.ROWS;
	
    public SimpleMatrix get_feature_vector(State temp_s, int a)
    {    
    	// Prepare to return the features
		double rows = (double) State.ROWS;
		double cols = (double) State.COLS;
		double[][] resTemp = new double[1][nFeatures];
		// Feature vector is height of each col, # of holes in each col, number of 
		// filled spaces in each row, height of tallest col, height of shortest col,
		// total number of holes, number of empty spaces below the highest point 
		// on the board, avg height of columns, number of filled spaces in emptiest 
		// row, number filled in fullest row
    	
    	// Look at the board after taking the action
		State s = new State(temp_s);
		int numRowsCleared = s.getRowsCleared();
		
		s.makeMove(a);
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
		
		//Get col heights
		int[] temp = s.getTop();
		int max = temp[0];
		//Store first just these heights and then the abs difference between consecutive heights
		for(int i=0; i<s.COLS; i++)
		{
			resTemp[0][i] = temp[i];
			if (i<s.COLS-1){
			resTemp[0][10+i] = Math.abs(temp[i] - temp[i+1]); 
			}
			if(temp[i]>max)
			{
				max = temp[i];
			}
		}
		resTemp[0][19] = max;
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
		}
		
		resTemp[0][20] = nHoles;
		//And adding a 1 for bias
		resTemp[0][21] = 1;
		
		
		// Return the features.  Can change which features are used by 
		// changing assignments to resTemp and nFeatures above
		//int curInd = 2*State.COLS + State.ROWS;
		int curInd = 0;
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
		Feature ret = new AbbeelFeature();
		return ret;
	}

}
