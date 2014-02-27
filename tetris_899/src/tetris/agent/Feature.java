package tetris.agent;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.*;

public interface Feature {
	
	// Return a feature vector for the state-action pair
	public SimpleMatrix get_feature_vector(State s, int a);

	// Return the dimensionality of features returned by this feature function
	public int get_feature_dimension();
	
	// Return a deep copy of this feature function
	public Feature copy();
}
