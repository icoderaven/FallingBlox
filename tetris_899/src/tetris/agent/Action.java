package tetris.agent;

import tetris.simulator.State;

/** A simple immutable class representing an action comprised of a move index. 
 * The fields are public gettable for convenience but not settable.
 * @author Humhu
 */
public class Action {

		public int index = -1;
		
		public Action(int ind)
		{
			index = ind;
		}
		
		public Action(Action other) {
			index = other.index;
		}
		
		/** 
		 * Apply this action to a state. This changes the state!
		 * @param state - The state to apply the action to.
		 */
		public void apply(State state) {
			state.makeMove(index);
		}
		
		public boolean is_fatal(State state) {
			State copyState = new State(state);
			apply(copyState);
			return copyState.hasLost();
		}

}
