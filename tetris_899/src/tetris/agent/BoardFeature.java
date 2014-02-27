package tetris.agent;

import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.*;


/**
 * Returns a vector of features based on the board after taking the given action.
 * @author eavrunin
 */

public class BoardFeature implements Feature {
		
//	public int nFeatures = 13 + 2*State.COLS - 1;
	public int nFeatures = 13;
//	public int nFeatures = 4;
	
    public SimpleMatrix get_feature_vector(State prev_s, int a)
    {    
    	long startTime = System.nanoTime();
    	
    	// Prepare to return the features
		double rows = (double) State.ROWS;
		double cols = (double) State.COLS;
		int curInd = 0;
		double[][] resTemp = new double[1][nFeatures];
    	
    	// Extract the board before taking the action
		State s = new State(prev_s);
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
		
		// Take the action
		s.makeMove(a);
		
		// If fatal move, return null
		if(s.hasLost()) {
			return null;
		}
		
		// Feature: number of rows cleared/eroded
    	int eroded = (s.getRowsCleared() - numRowsCleared)*State.COLS;
    	resTemp[0][curInd++] = eroded;
		
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
		
		// Height of tallest column, shortest column
		int[] temp = s.getTop();
		Arrays.sort(temp);
		
		int maxH = temp[temp.length-1];
		resTemp[0][curInd++] = maxH;
		int minH = temp[0];
		resTemp[0][curInd++] = minH;

		int[] colHeight = s.getTop();
		int totalHeight = 0;
		
		// Number of holes
		int nHoles = 0;
		int[] colHoles = new int[State.COLS];
		

		// Number of empty slots below the top row
		//double nEmptyBelow = 0;
		
		for(int i=0; i < s.COLS; i++) {
			// Column heights
			int h = colHeight[i];
//			resTemp[0][curInd++] = h;
			
			SimpleMatrix col = board.extractVector(false, i);
			double nFullCol = col.elementSum();
			colHoles[i] = (int)(h - nFullCol);
			nHoles += colHoles[i];
			
			//nEmptyBelow += maxH - nFullCol;
			totalHeight += h;
		}
		
		// Mean column height
		double avgH = totalHeight / cols;
		resTemp[0][curInd++] = avgH;
		// Max - mean height
		double maxAvgDiff = maxH - avgH;
		resTemp[0][curInd++] = maxAvgDiff;
		// Mean - min height
		double minAvgDiff = avgH - minH;
		resTemp[0][curInd++] = minAvgDiff;		

		// Height inertia
		double hInertia = 0;
		for(int i = 0; i < State.COLS; i++) {
			for(int j = 0; j < State.ROWS; j++) {
				hInertia += board.get(j,i)*j;
			}
		}
		resTemp[0][curInd++] = hInertia;
		
		
		// Size of most-filled row, least, average, average below maxH
		//double[] rowFill = new double[s.ROWS];
		//double totalRowFill = 0.0;
		//for(int i=0; i < s.ROWS; i++) {
			//SimpleMatrix row = board.extractVector(true, i);
			//double nFullRow = row.elementSum();
			//rowFill[i] = nFullRow;
			//totalRowFill += nFullRow;
			
			//resTemp[0][2*State.COLS + i] = nFullRow/cols;
		//}
		//Arrays.sort(rowFill);
		//double minRow = rowFill[0];
		//double maxRow = rowFill[rowFill.length - 1];
		//double avgRow = totalRowFill / s.ROWS;
		//double avgRowFilled = totalRowFill / maxH;  // could divide by zero
		
		// Rows removed
		//int rGone = s.getRowsCleared();
		
		// Number of filled spaces in top row
		//SimpleMatrix highestRow = board.extractVector(true, (int)maxH);
		//double nHighestRow = highestRow.elementSum();
		
		// Features from: http://hal.archives-ouvertes.fr/docs/00/41/89/54/PDF/article.pdf
		
		// Column differences
		double totalHDiff = 0.0;
		for(int i = 0; i < State.COLS - 1; i++) {
			double d = Math.abs(colHeight[i] - colHeight[i+1]);
//			resTemp[0][curInd++] = d;
			totalHDiff += d;
		}
		resTemp[0][curInd++] = totalHDiff;
		
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
		resTemp[0][curInd++] = numFlips;
		
		// Rows with holes
		int nRowsHoles = 0;
		for(int i = 0; i < State.ROWS; i++) {
			SimpleMatrix row = board.extractVector(true, i);
			if(row.elementSum() != State.COLS) {
				nRowsHoles++;
			}
		}
		resTemp[0][curInd++] = nRowsHoles;
		
		// Hole depth/number of overhanging full cells
		int nOverhang = 0;
		for(int j = 0; j < State.COLS; j++) {
			SimpleMatrix col = board.extractVector(false, j);
			
			if(colHoles[j] != 0) {
				int nHolesSeen = 0;
				for(int i = 0; i < colHeight[j]; i++) {
					if(col.get(i) == 0) {  // i.e., in a hole
						nHolesSeen++;
						nOverhang += (colHeight[j] - i) - (colHoles[j] - nHolesSeen);
					}
				}
			}
		}
		resTemp[0][curInd++] = nOverhang;

		
		// Number of filled spaces in top four rows
//		SimpleMatrix topFourRows = board.extractMatrix(State.ROWS - 4, State.ROWS, 0, State.COLS);
//		double topRows = topFourRows.elementSum();
		
		// Number of filled spaces in all rows
		double numFilled = board.elementSum();
		resTemp[0][curInd++] = numFilled;
		
		resTemp[0][curInd++] = nHoles;
		
		// Return the features.  Can change which features are used by 
		// changing assignments to resTemp and nFeatures above
		
		//double[] indivFeatures =  {maxH, numFlips, nHoles, 
		//	nEmptyBelow, avgH, hInertia, maxRow, erodedRows, numFilled};
		//System.arraycopy(indivFeatures, 0, resTemp[0], curInd, indivFeatures.length);
		
		SimpleMatrix res = new SimpleMatrix(resTemp);
		res = res.transpose();
		
		long endTime = System.nanoTime();
		//System.out.println("Feature calculation took " + ((endTime - startTime) / 1000000) + "ms");

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
