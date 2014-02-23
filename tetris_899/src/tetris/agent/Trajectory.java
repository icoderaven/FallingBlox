package tetris.agent;
import tetris.simulator.*;

import java.util.Iterator;
import java.util.Vector;

public class Trajectory {
	
	public class SARTuple {
	    final public State state;
	    final public Action action;
	    final public double reward;

	    //Link constructor
	    public SARTuple(State s, Action a, double r) {
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
	
	public Trajectory() {
		tuples = new Vector<SARTuple>();
	};
	
	// Need to make a deep copy. Damn you Java.
	public Trajectory(Trajectory traj) {
		tuples = new Vector<SARTuple>(traj.tuples.size());
		Iterator<SARTuple> iter = traj.tuples.iterator();
		while(iter.hasNext()) {
			SARTuple sar = new SARTuple(iter.next());
			tuples.add(sar);
		}
	}
	
	// Another deep copy method to be interface consistent
	public Trajectory copy() {
		Trajectory ret = new Trajectory(this);
		return ret;
	}
	
	public boolean add(State s, Action a, double r) {
		SARTuple sar = new SARTuple(s, a, r);
		return tuples.add(sar);
	}
	
	public double sum_rewards() {
		double sum = 0;
		Iterator<SARTuple> iterator = tuples.iterator();
		while(iterator.hasNext()) {
			sum += iterator.next().reward;
		}
		return sum;
	}
	
	public double sum_rewards(int start_index) {
		double sum = 0;
		for(int i=start_index; i<tuples.size(); i++)
			sum+=tuples.get(i).reward;
		return sum;
	}
	
}