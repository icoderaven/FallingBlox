package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;

/**
 * Returns a vector of features based on the board after taking the given
 * action.
 * 
 * @author eavrunin
 */

public class TopFourFeatures implements Feature {

	public int nFeatures = 4 * State.COLS+1; // + 2*State.COLS + State.ROWS;

	public SimpleMatrix get_feature_vector(State temp_s, Action a) {
		// Prepare to return the features
		double rows = (double) State.ROWS;
		double cols = (double) State.COLS;
		double[][] resTemp = new double[1][nFeatures];
		// Feature vector is height of each col, # of holes in each col, number
		// of
		// filled spaces in each row, height of tallest col, height of shortest
		// col,
		// total number of holes, number of empty spaces below the highest point
		// on the board, avg height of columns, number of filled spaces in
		// emptiest
		// row, number filled in fullest row

		// Look at the board after taking the action
		State s = new State(temp_s);
		int numRowsCleared = s.getRowsCleared();

		s.makeMove(a.index);

		// Extract the board and set all nonzero values to 1
		SimpleMatrix board = new SimpleMatrix(State.ROWS, State.COLS);
		int[][] field = s.getField();
		for (int i = 0; i < s.ROWS; i++) {
			for (int j = 0; j < s.COLS; j++) {
				board.set(i, j, (double) Math.min(field[i][j], 1));
			}
		}

		// Compute the features:
		// Height of tallest column, shortest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		int maxH = temp[temp.length - 1]-1;
		// Just store the top 4 rows
		int ctr=0;
		for (int i = maxH; i >= maxH - 3; i--) {
			for (int j = 0; j < s.COLS; j++) {
				double val = 0.0;
				if(i>=0){
					val = board.get(i,j);
				}
				resTemp[0][ctr * s.COLS + j] = val;
			}
			ctr+=1;
		}
		
		resTemp[0][nFeatures-2] = (double)maxH;
		resTemp[0][nFeatures-1] = 1.0;
		SimpleMatrix res = new SimpleMatrix(resTemp);
		res = res.transpose();
		// res.print();
		return res;
	}

	// TODO Update as necessary
	public int get_feature_dimension() {
		return nFeatures;
	}

	// TODO Update as necessary
	public Feature copy() {
		Feature ret = new TopFourFeatures();
		return ret;
	}

}
