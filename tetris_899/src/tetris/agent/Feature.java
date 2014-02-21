package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;


public interface Feature {
	public SimpleMatrix get_feature_vector(State s, Action a);
}
