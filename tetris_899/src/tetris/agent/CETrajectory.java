package tetris.agent;
import tetris.simulator.*;

import org.ejml.simple.SimpleMatrix;

import java.util.Iterator;
import java.util.Vector;

public class CETrajectory {
	
	public class SARTuple {
	    final public State state;
	    final public int action;
	    final public double reward;

	    //Link constructor
	    public SARTuple(State s, int a, double r) {
	    	state = new State(s);
	    	action = a;
	    	reward = r;
	    }
	    
	    public SARTuple(SARTuple sar) {
	    	state = new State(sar.state);
	    	action = sar.action;
	    	reward = sar.reward;
	    }
	    
	}
	
	public Vector<SARTuple> tuples;
	public SimpleMatrix params;
	
	public CETrajectory() {
		tuples = new Vector<SARTuple>();
		params = new SimpleMatrix(13, 1); // hardcoded to the current feature set for simplicity
	};
	
	// Need to make a deep copy. Damn you Java.
	public CETrajectory(CETrajectory traj) {
		tuples = new Vector<SARTuple>(traj.tuples.size());
		Iterator<SARTuple> iter = traj.tuples.iterator();
		while(iter.hasNext()) {
			SARTuple sar = new SARTuple(iter.next());
			tuples.add(sar);
		}
		
		params = new SimpleMatrix(traj.params);
	}
	
	// Another deep copy method to be interface consistent
	public CETrajectory copy() {
		CETrajectory ret = new CETrajectory(this);
		return ret;
	}
	
//	public int compareTo(CETrajectory other) {
//		double a = sum_rewards_tail(0, 1.0);
//		double b = other.sum_rewards_tail(0, 1.0);
//        return ((Double) a).compareTo(b);
//	}
	
	public void setParams(SimpleMatrix p) {
		params = new SimpleMatrix(p);
	}
	
	public boolean add(State s, int a, double r) {
		SARTuple sar = new SARTuple(s, a, r);
		return tuples.add(sar);
	}
	
	public double sum_rewards_tail(int start_index, double gamma) {
		return sum_rewards(start_index, tuples.size() - 1, gamma);
	}
	
	public double sum_rewards_head(int end_index, double gamma) {
		return sum_rewards(0, end_index, gamma);
	}
	
	public double sum_rewards(int start_index, int end_index, double gamma) {
		double sum = 0, discount = 1.0;
		
		if(start_index < 0) { start_index = 0; }
		if(end_index > tuples.size() - 1) { end_index = tuples.size() - 1; }
		
		for(int i = start_index; i <= end_index; i++) {
			sum += discount*tuples.get(i).reward;
			discount = discount * gamma;
		}
		return sum;
	}
	
}