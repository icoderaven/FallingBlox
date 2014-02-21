package tetris.agent;
import tetris.simulator.*;

import java.util.Iterator;
import java.util.Vector;

public class Trajectory {
	
	public class SARTuple implements Cloneable {
	    public State _state;
	    public Action _action;
	    public double _reward;

	    //Link constructor
	    public SARTuple(State s, Action a, double r) {
	    	_state = new State(s);
	    	_action = a;
	    	_reward = r;
	    }
	    
	    public SARTuple(SARTuple sar) {
	    	_state = new State(sar._state);
	    	_action = sar._action;
	    	_reward = sar._reward;
	    }
	    
	}
	
	public Vector<SARTuple> _trajectory;
	
	public Trajectory() {};
	
	// Need to make a deep copy. Damn you Java.
	public Trajectory(Trajectory traj) {
		Iterator<SARTuple> iter = traj._trajectory.iterator();
		while(iter.hasNext()) {
			_trajectory.add(iter.next());
		}
	}
	
	public boolean Add(State s, Action a, double r) {
		SARTuple sar = new SARTuple(s, a, r);
		return _trajectory.add(sar);
	}
}