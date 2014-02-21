package tetris.agent;
import java.util.Vector;

import tetris.simulator.*;

class SARTuple {
    public State _state;
    public Action _action;
    public double _reward;

    //Link constructor
    public SARTuple(State s, Action a, double r) {
    	_state =s;
    	_action =a;
    	_reward = r;
    }
    
}

public class Trajectory {
	public Vector<SARTuple> _trajectory;
}