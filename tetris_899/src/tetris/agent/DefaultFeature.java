package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;

public class DefaultFeature implements Feature {

	@Override
	public SimpleMatrix get_feature_vector(State temp_s, int a) {
		State s = new State(temp_s);
		s.makeMove(a);
		SimpleMatrix temp = new SimpleMatrix(State.COLS*State.ROWS, 1) ;
		for(int i=0; i< s.ROWS; i++)
		{
			for(int j=0; j< s.COLS; j++)
			{
				temp.set(i*s.COLS+j ,  s.getField()[i][j]>0?1:0);
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
