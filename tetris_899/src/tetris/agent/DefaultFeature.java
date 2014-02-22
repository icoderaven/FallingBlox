package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;

public class DefaultFeature implements Feature {

	@Override
	public SimpleMatrix get_feature_vector(State temp_s, Action a) {
		State s = new State(temp_s);
		s.makeMove(a.index);
		SimpleMatrix temp = new SimpleMatrix(State.COLS*State.ROWS, 1) ;
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				temp.set(i*temp.numCols()+j ,  s.getField()[i][j]);
			}
		}
		
		return temp;
	}

	@Override
	public int get_feature_dimension() {
		return State.COLS*State.ROWS;
	}

	@Override
	public Feature copy() {
		// TODO Auto-generated method stub
		return new DefaultFeature();
	}

}
