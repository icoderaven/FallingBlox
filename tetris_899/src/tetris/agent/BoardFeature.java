package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeature implements Feature {
		
	public int nFeatures = 9 + 2*State.COLS - 1;
	
    public SimpleMatrix get_feature_vector(State temp_s, Action a)
    {    
    	// Prepare to return the features
		double rows = (double) State.ROWS;
		double cols = (double) State.COLS;
		int curInd = 0;
		double[][] resTemp = new double[1][nFeatures];
		// Feature vector is height of each col, # of holes in each col, number of 
		// filled spaces in each row, height of tallest col, height of shortest col,
		// total number of holes, number of empty spaces below the highest point 
		// on the board, avg height of columns, number of filled spaces in emptiest 
		// row, number filled in fullest row, avg of rows
    	
    	// Look at the board after taking the action
		State s = new State(temp_s);
		int numRowsCleared = s.getRowsCleared();
		
		SimpleMatrix prevBoard = new SimpleMatrix(State.ROWS, State.COLS) ;
		int[][] prevField = s.getField();
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				prevBoard.set(i, j,  (double)Math.min(prevField[i][j], 1));
			}
		}
		
		// Get board afterwards
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
			double nFullCol = col.elementSum();
			nHoles += h - nFullCol;
			colHoles[i] = h - nFullCol;
			nEmptyBelow += maxH - nFullCol;
			totalHeight += h;
			
			//resTemp[0][i] = h/rows;
			//resTemp[0][State.COLS + i] = (h - nFullCol)/rows;
		}
		
		double avgH = totalHeight / s.COLS;

		// Size of most-filled row, least, average, average below maxH
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
		//double avgRowFilled = totalRowFill / maxH;  // could divide by zero
		
		// Rows removed
		//int rGone = s.getRowsCleared();
		
		// Number of filled spaces in top row
		SimpleMatrix highestRow = board.extractVector(true, (int)maxH);
		double nHighestRow = highestRow.elementSum();
		
		// Features from: http://hal.archives-ouvertes.fr/docs/00/41/89/54/PDF/article.pdf
		
		// Column differences
		for(int i = 0; i < State.COLS - 1; i++) {
			resTemp[0][curInd++] = Math.abs(colHeight[i] - colHeight[i+1]);
		}
		
		// Column Heights
		for(int i = 0; i < State.COLS; i++) {
			resTemp[0][curInd++] = colHeight[i];
		}
		
		// Landing height of last piece
		// Not sure how to quickly calculate this
		
		// Cell transitions
		int numFlips = 0;
		for(int i = 0; i < State.COLS; i++) {
			double prevBlock = board.get(0,i);
			for(int j = 1; j < State.ROWS; j++) {
				double nextBlock = board.get(j,i);
				if(prevBlock != nextBlock) {
					numFlips++;
				}
				prevBlock = nextBlock;
			}
		}
		
		// Height inertia
		double hInertia = 0;
		for(int i = 0; i < State.COLS; i++) {
			for(int j = 0; j < State.ROWS; j++) {
				hInertia += board.get(j,i)*j;
			}
		}
		
		// Number of filled spaces in top four rows
//		SimpleMatrix topFourRows = board.extractMatrix(State.ROWS - 4, State.ROWS, 0, State.COLS);
//		double topRows = topFourRows.elementSum();
		
		// Number of filled spaces in all rows
		double numFilled = board.elementSum();
		
		// Return the features.  Can change which features are used by 
		// changing assignments to resTemp and nFeatures above
		
		double[] indivFeatures =  {maxH, numFlips, nHoles, 
			nEmptyBelow, avgH, hInertia, maxRow, erodedRows, numFilled};
		System.arraycopy(indivFeatures, 0, resTemp[0], curInd, indivFeatures.length);
		SimpleMatrix res = new SimpleMatrix(resTemp);
		res = res.transpose();

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
