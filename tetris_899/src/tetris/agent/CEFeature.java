package tetris.agent;

import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;

import org.ejml.simple.SimpleMatrix;

/**
 * Returns a short and hopefully not too correlated vector of features for Cross-Entropy.
 * @author eavrunin
 */

public class CEFeature implements Feature {

	public int nFeatures = 2;
	
	@Override
	public SimpleMatrix get_feature_vector(State prev_s, int a) {
    	// Extract the board before taking the action
		State s = new State(prev_s);
		int numRowsCleared = s.getRowsCleared();
		
		// Take the action
		s.makeMove(a);
		
//		// If fatal move, return null
//		if(s.hasLost()) {
//			return null;
//		}
//		
//		// Feature: number of rows cleared/eroded
//    	int eroded = (s.getRowsCleared() - numRowsCleared)*State.COLS;
		
    	// Extract the new board and set all nonzero values to 1
		SimpleMatrix board = new SimpleMatrix(State.ROWS, State.COLS) ;
		int[][] field = s.getField();
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				board.set(i, j,  (double)Math.min(field[i][j], 1));
			}
		}
		
		// Height of tallest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		int maxH = temp[temp.length-1];
		
		// Number of holes
		double nHoles = 0;		
		int[] colHeight = s.getTop();
		for(int i=0; i < State.COLS; i++) {
			// Column heights
			int h = colHeight[i];			
			SimpleMatrix col = board.extractVector(false, i);
			nHoles += h - col.elementSum();
		}
		
		double[][] resArray = {{maxH, nHoles}};
		SimpleMatrix res = new SimpleMatrix(resArray);
		
		return res.transpose();
	}

	@Override
	public int get_feature_dimension() {
		return nFeatures;
	}

	@Override
	public Feature copy() {
		Feature ret = new CEFeature();
		return ret;
	}

}
